package controller;

//Contrato de persistência do planejamento: qualquer forma de armazenamento (arquivo, banco...)
//deve implementar as operações de salvar e carregar
public interface Persistencia {

    //Salvar o sistema completo (eventos, participantes, despesas e seus vínculos)
    void salvar(Controle sistema) throws PersistenciaException;

    //Carregar o sistema salvo; em caso de falha lança exceção e não altera o sistema em uso
    Controle carregar() throws PersistenciaException;
}
