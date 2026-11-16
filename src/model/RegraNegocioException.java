package model;

import java.io.Serial;

//Exceção lançada pelo model quando uma entrada ou operação viola uma regra do planejamento
//(campo vazio, valor inválido, código duplicado, despesa sem participantes, exclusão não permitida...)
public class RegraNegocioException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
