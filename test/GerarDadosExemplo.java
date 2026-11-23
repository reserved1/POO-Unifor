import controller.Controle;
import controller.GerenciadorArquivo;

import java.math.BigDecimal;
import java.util.List;

//Gera os arquivos de exemplo com o caso de referência da seção 4 (passeio da turma),
//já classificado para o simulador da seção 7 (transporte e almoço essenciais; atividade
//opcional com prioridade 3; sobremesa opcional com prioridade 1). A classificação não muda
//os valores do caso de referência: Ana 75,00, Bruno 60,00, Carla 75,00, total 210,00.
//  dados/dados.dat            -> arquivo lido pelo programa ao iniciar
//  dados/caso-referencia.dat  -> cópia de segurança para restaurar o caso de referência
//Executar a partir da pasta do projeto (a mesma pasta onde fica "dados").
public class GerarDadosExemplo {

    public static void main(String[] args) throws Exception {
        Controle sistema = new Controle();
        int ev = sistema.addEvento("Passeio da turma").getId();

        sistema.addParticipante(ev, 1, "Ana", new BigDecimal("70.00"));
        sistema.addParticipante(ev, 2, "Bruno", new BigDecimal("60.00"));
        sistema.addParticipante(ev, 3, "Carla", new BigDecimal("70.00"));

        // tipo 1 = Grupo (valor fixo dividido) | tipo 0 = Individual (valor por pessoa)
        // despesa essencial inclui automaticamente todos os participantes
        sistema.salvarDespesa(ev, 1, "Transporte", new BigDecimal("90.00"), true, 0, 1, null);
        sistema.salvarDespesa(ev, 2, "Almoço", new BigDecimal("20.00"), true, 0, 0, null);
        sistema.salvarDespesa(ev, 3, "Atividade recreativa", new BigDecimal("30.00"), false, 3, 1, List.of(1, 3));
        sistema.salvarDespesa(ev, 4, "Sobremesa", new BigDecimal("10.00"), false, 1, 0, List.of(1, 2, 3));

        new GerenciadorArquivo("dados/dados.dat").salvar(sistema);
        new GerenciadorArquivo("dados/caso-referencia.dat").salvar(sistema);
        System.out.println("Arquivos dados/dados.dat e dados/caso-referencia.dat gerados. Custo total: R$ " + sistema.calcularTotalEvento(ev));
    }
}
