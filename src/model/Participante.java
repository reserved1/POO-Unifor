package model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Participante implements Serializable {
    @Serial
    //Implementar a Serializable da classe
    private static final long serialVersionUID = 1L;

    // Atributos
    private final int id;
    private String nome;
    private BigDecimal orcamento;
    private Map<Integer,Despesas> listaDespesas = new HashMap<>();


    //Metodo para adicionar uma despesas a lista do participante
    public void adicionarDespesas(Despesas despesaAdicionar){
        listaDespesas.put(despesaAdicionar.getId(), despesaAdicionar);
    }

    //Metodo para remover uma despesas a lista do participante
    public boolean removerDespesas(int idDespesaRemover){
        if (listaDespesas.containsKey(idDespesaRemover)) {
            return listaDespesas.remove(idDespesaRemover) != null;
        }else {
            return true;
        }
    }

    // construtor de participante (valida os dados antes de criar o objeto)
    public Participante(int id, String nome, BigDecimal orcamento) {
        validarNome(nome);
        validarOrcamento(orcamento);
        this.id = id;
        this.nome = nome.trim();
        this.orcamento = orcamento;
    }

    //Metodo para validar o nome do participante
    public static void validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraNegocioException("O nome do participante não pode ser vazio.");
        }
    }

    //Metodo para validar o orçamento do participante (maior ou igual a zero e no maximo 2 casas decimais)
    public static void validarOrcamento(BigDecimal orcamento) {
        if (orcamento == null) {
            throw new RegraNegocioException("O orçamento do participante não pode ser vazio.");
        }
        if (orcamento.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("O orçamento do participante deve ser maior ou igual a zero.");
        }
        if (orcamento.stripTrailingZeros().scale() > 2) {
            throw new RegraNegocioException("O orçamento deve ter no máximo duas casas decimais.");
        }
    }

    // Get e Set do participante
    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        validarNome(nome);
        this.nome = nome.trim();
    }

    public BigDecimal getOrcamento() {
        return orcamento;
    }

    public void setOrcamento(BigDecimal orcamento) {
        validarOrcamento(orcamento);
        this.orcamento = orcamento;
    }

    //Retorna a lista de despesas somente para leitura (a alteração é feita pelos metodos adicionar/remover)
    public Map<Integer, Despesas> getListaDespesas() {
        return Collections.unmodifiableMap(listaDespesas);
    }

}
