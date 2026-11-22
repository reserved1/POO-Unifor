package controller;

import java.io.Serial;

//Exceção para falhas ao salvar ou carregar o arquivo de dados
public class PersistenciaException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    //Indica se a falha foi causada pela ausência do arquivo
    private final boolean arquivoAusente;

    public PersistenciaException(String mensagem, boolean arquivoAusente, Throwable causa) {
        super(mensagem, causa);
        this.arquivoAusente = arquivoAusente;
    }

    public boolean isArquivoAusente() {
        return arquivoAusente;
    }
}
