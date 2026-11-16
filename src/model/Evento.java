package model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Evento {
    // Atributos
    private final int id;
    private String nome;
    private Map<Integer,Participante> listaParticipantes = new HashMap<>();
    private Map<Integer,Despesas> listaDespesas = new HashMap<>();

    //Metodo para adicionar um participante a lista do evento
    public void adicionarParticipante(Participante participanteAdicionar){
        //Verificar se o participante esta na lista, senão adicionar a lista
        if (!listaParticipantes.containsValue(participanteAdicionar)) {
            listaParticipantes.put(participanteAdicionar.getId(), participanteAdicionar);
        }


    }

    //Metodo para adicionar uma despesa a lista do evento
    public void adicionarDespesa(Despesas despesaAdicionar){
        //Verificar se o objeto esta na lista, senão adicionar a lista e se essencial adicionar a todos os participantes
        if (!listaDespesas.containsValue(despesaAdicionar)) {
            listaDespesas.put(despesaAdicionar.getId(), despesaAdicionar);
        }

        //Verificar se a despesas esta na lista de despesas do participantes
        if (!listaParticipantes.isEmpty()){
            for (Participante participante : listaParticipantes.values()){
                if (participante.getListaDespesas().containsKey(despesaAdicionar.getId())) {
                    //Se os dados da despesa diferente da lista do participante atualizar a despesa
                    if (!participante.getListaDespesas().containsValue(despesaAdicionar)) {
                        participante.adicionarDespesas(despesaAdicionar);
                    }
                }
            }
        }


    }

    //Metodo para remover um participante da lista do evento
    public boolean removerParticipante(int idParticipante) {
        return listaParticipantes.remove(idParticipante) != null;
    }

    //Metodo para remover uma despesa da lista do evento
    public boolean removerDespesa(int idDespesa) {
        return listaDespesas.remove(idDespesa) != null;
    }

    // construtor do evento
    public Evento(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    // Get e set do evento
    // Retornar a lista de participantes do evento (somente leitura)
    public Map<Integer,Participante> getListaParticipantes() {
        return Collections.unmodifiableMap(listaParticipantes);
    }

    // Retornar a lista de despesas do evento (somente leitura)
    public Map<Integer,Despesas> getListaDespesas() {
        return Collections.unmodifiableMap(listaDespesas);
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

}
