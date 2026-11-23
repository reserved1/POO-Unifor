package controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

//Implementação do contrato de persistência usando serialização de objetos em arquivo local
public class GerenciadorArquivo implements Persistencia {

    //Caminho padrão do arquivo de dados
    public static final String CAMINHO_PADRAO = "dados/dados.dat";

    private final File arquivo;

    //Construtor com o caminho padrão
    public GerenciadorArquivo() {
        this(CAMINHO_PADRAO);
    }

    //Construtor com outro caminho de arquivo
    public GerenciadorArquivo(String caminhoFicheiro) {
        this.arquivo = new File(caminhoFicheiro);
    }

    //Metodo para salvar o sistema em arquivo
    @Override
    public void salvar(Controle sistemaSalvar) throws PersistenciaException {

        // Cria a pasta se não existir
        if (arquivo.getParentFile() != null) {
            arquivo.getParentFile().mkdirs();
        }

        //Grava primeiro em um arquivo temporário para não corromper o arquivo atual em caso de falha
        File temporario = new File(arquivo.getPath() + ".tmp");

        //Gravar o sistema salvar em arquivo (try-with-resources fecha o fluxo automaticamente)
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(temporario))) {
            oos.writeObject(sistemaSalvar);
        } catch (IOException e) {
            temporario.delete();
            throw new PersistenciaException("Não foi possível salvar os dados em " + arquivo.getPath() + ".\nMotivo: " + e.getMessage(), false, e);
        }

        //Substituir o arquivo antigo pelo novo
        try {
            Files.move(temporario.toPath(), arquivo.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível substituir o arquivo " + arquivo.getPath() + ".\nMotivo: " + e.getMessage(), false, e);
        }
    }

    //Metodo para carregar o sistema do arquivo e retornando ele
    @Override
    public Controle carregar() throws PersistenciaException {

        //Verificar se o arquivo existe
        if (!arquivo.exists()) {
            throw new PersistenciaException("Arquivo de dados não encontrado: " + arquivo.getPath() + ".", true, null);
        }

        //Retorna o sistema salvo no arquivo (try-with-resources fecha o fluxo automaticamente)
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo))) {
            Object lido = ois.readObject();
            if (!(lido instanceof Controle)) {
                throw new PersistenciaException("O arquivo " + arquivo.getPath() + " não contém um planejamento válido.", false, null);
            }
            return (Controle) lido;
        } catch (InvalidClassException e) {
            throw new PersistenciaException("O arquivo " + arquivo.getPath() + " foi salvo por uma versão incompatível do programa.", false, e);
        } catch (StreamCorruptedException | EOFException e) {
            throw new PersistenciaException("O arquivo " + arquivo.getPath() + " está corrompido ou não é um arquivo de dados do programa.", false, e);
        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenciaException("Não foi possível ler o arquivo " + arquivo.getPath() + ".\nMotivo: " + e.getMessage(), false, e);
        }
    }

}
