package model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class Despesas implements Serializable {
    @Serial
    //Implementar a Serializable da classe
    private static final long serialVersionUID = 1L;

    // Atributos
    private final int id;
    private String nome;
    private BigDecimal valor;
    private boolean essencial;
    private int prioridade;
    private int tipo;
    //Lista de participantes da despesas com o valor da despesa por participante (Integer id Participante , Big decimal valor da despesa do participante)
    //protected para que as subclasses (DespesasGrupo e DespesasIndividual) façam o calculo das parcelas
    protected Map<Integer, BigDecimal> participatesDespesas = new HashMap<>();

    // Metodo abstrato para fazer o calculo da despesa
    public abstract void calcularDespesas();

    // Metodo abstrato para adicionar um participante a lista da despesa
    public abstract void adicionarParticipante(int idParticipanteAdicionar);

    // Metodo abstrato para remover um participante a lista da despesa
    public abstract boolean removerParticipante(int idParticipanteRemover);

    // construtor de despesa (valida os dados antes de criar o objeto)
    public Despesas(int id, String nome, BigDecimal valor, boolean essencial, int prioridade, int tipo) {
        validarNome(nome);
        validarPrioridade(prioridade);
        this.id = id;
        this.nome = nome.trim();
        this.valor = validarValor(valor);
        this.essencial = essencial;
        this.prioridade = prioridade;
        this.tipo = tipo;
    }

    //Metodo para validar o nome da despesa
    public static void validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraNegocioException("A descrição da despesa não pode ser vazia.");
        }
    }

    //Metodo para validar o valor da despesa (maior que zero e no maximo 2 casas decimais) e retornar com 2 casas
    public static BigDecimal validarValor(BigDecimal valor) {
        if (valor == null) {
            throw new RegraNegocioException("O valor da despesa não pode ser vazio.");
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O valor da despesa deve ser maior que zero.");
        }
        try {
            return valor.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new RegraNegocioException("O valor da despesa deve ter no máximo duas casas decimais.");
        }
    }

    //Metodo para validar a prioridade da despesa
    public static void validarPrioridade(int prioridade) {
        if (prioridade < 0) {
            throw new RegraNegocioException("Prioridade inválida: escolha um número inteiro de 0 a 9.");
        }
    }

    // Get e Set da despesa
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

    public BigDecimal getValor() {
        return valor;
    }

    public boolean isEssencial() {
        return essencial;
    }

    public int getPrioridade() {
        return prioridade;
    }

    public int getTipo() {
        return tipo;
    }

    //Retorna a lista de participantes somente para leitura (a alteração é feita pelos metodos adicionar/remover)
    public Map<Integer, BigDecimal>  getListaParticipantes(){

        return Collections.unmodifiableMap(participatesDespesas);
    }

    //Retorna o valor que o participante paga nesta despesa (zero se ele não participa)
    public BigDecimal getValorParticipante(int idParticipante) {
        return participatesDespesas.getOrDefault(idParticipante, new BigDecimal("0.00"));
    }

    //Retorna a soma das parcelas de todos os participantes da despesa
    public BigDecimal getTotalParcelas() {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal parcela : participatesDespesas.values()) {
            total = total.add(parcela);
        }
        return total;
    }

}
