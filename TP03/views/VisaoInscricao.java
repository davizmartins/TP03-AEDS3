package TP03.views;

import TP03.models.*;
import java.util.Scanner;
import java.time.format.DateTimeFormatter;

public class VisaoInscricao {
    private Scanner scanner = new Scanner(System.in);
    private static final int ITENS_POR_PAGINA = 10;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private int paginaAtual = 1;
    private Curso[] cursosPaginados;
    private int totalPaginas;

    /**
     * Menu principal de inscricoes
     */
    public void mostrarMenuInscricoes() {
        System.out.println("\n       EntrePares 1.0                  ");
        System.out.println("========================================");
        System.out.println(" > Inicio > Minhas inscricoes          ");
        System.out.println("");
        System.out.println(" (A) Buscar curso por codigo           ");
        System.out.println(" (B) Buscar curso por palavras-chave   ");
        System.out.println(" (C) Listar todos os cursos            ");
        System.out.println(" (R) Retornar ao menu anterior         ");
        System.out.print("Opcao: ");
    }

    /**
     * Mostra a tela de Minhas Inscricoes com lista e opcoes de acao
     */
    public void mostrarTelaMinhasInscricoes(Curso[] cursos) {
        System.out.println("\n       EntrePares 1.0                  ");
        System.out.println("========================================");
        System.out.println(" > Inicio > Minhas inscricoes          ");
        System.out.println("");

        if (cursos == null || cursos.length == 0) {
            System.out.println(" Voce nao tem inscricoes registradas.");
        } else {
            System.out.println(" Inscricoes atuais:");
            System.out.println("");
            for (int i = 0; i < Math.min(cursos.length, 10); i++) {
                String estado = obterEstadoCurso(cursos[i].getEstado());
                System.out.println(" (" + (i + 1) + ") " + cursos[i].getNome() + " - " + cursos[i].getDataInicio().format(FORMATO_DATA) + " " + estado);
            }
        }

        System.out.println("");
        System.out.println("----------------------------------------");
        System.out.println(" Opcoes:");
        System.out.println(" (A) Buscar curso por codigo           ");
        System.out.println(" (B) Buscar curso por palavras-chave   ");
        System.out.println(" (C) Listar todos os cursos            ");
        System.out.println(" (R) Retornar ao menu anterior         ");
        System.out.print("Opcao: ");
    }

    /**
     * Exibe lista de cursos com paginacao
     */
    public void mostrarListaCursos(Curso[] cursos, String titulo) {
        if (cursos == null || cursos.length == 0) {
            System.out.println("\n       EntrePares 1.0                  ");
            System.out.println("========================================");
            System.out.println(" > Inicio > Minhas inscricoes > " + titulo);
            System.out.println("\n Nenhum curso encontrado.");
            return;
        }

        this.cursosPaginados = cursos;
        this.totalPaginas = (int) Math.ceil((double) cursos.length / ITENS_POR_PAGINA);

        if (paginaAtual > totalPaginas) {
            paginaAtual = totalPaginas;
        }

        exibirPagina(titulo);
    }

    /**
     * Exibe a pagina atual
     */
    private void exibirPagina(String titulo) {
        System.out.println("\n       EntrePares 1.0                  ");
        System.out.println("========================================");
        System.out.println(" > Inicio > Minhas inscricoes > " + titulo);
        System.out.println("\nPagina " + paginaAtual + " de " + totalPaginas + "\n");

        int indiceInicio = (paginaAtual - 1) * ITENS_POR_PAGINA;
        int indiceFim = Math.min(indiceInicio + ITENS_POR_PAGINA, cursosPaginados.length);

        for (int i = indiceInicio; i < indiceFim; i++) {
            int numeroItem = i - indiceInicio;
            Curso c = cursosPaginados[i];
            String estado = obterEstadoCurso(c.getEstado());
            System.out.println("(" + numeroItem + ") " + c.getNome() + " - " + c.getDataInicio().format(FORMATO_DATA) + " " + estado);
        }

        if (totalPaginas > 1) {
            System.out.println("\n----------------------------------------");
            System.out.println(" (A) Pagina anterior");
            System.out.println(" (B) Proxima pagina");
        } else {
            System.out.println("\n----------------------------------------");
        }
        System.out.println(" (R) Retornar ao menu anterior");
        System.out.print("\nOpcao: ");
    }

    /**
     * Retorna o estado do curso como string
     */
    private String obterEstadoCurso(byte estado) {
        switch (estado) {
            case Curso.ATIVO_INSCRICOES:
                return "";
            case Curso.ATIVO_SEM_INSCRICOES:
                return "(INSCRICOES ENCERRADAS)";
            case Curso.CONCLUIDO:
                return "(CONCLUIDO)";
            case Curso.CANCELADO:
                return "(CANCELADO)";
            default:
                return "";
        }
    }

    /**
     * Mostra detalhes de um curso para inscricao
     */
    public void mostrarDetalheCursoParaInscricao(Curso curso, String nomeAutor) {
        System.out.println("\n       EntrePares 1.0                  ");
        System.out.println("========================================");
        System.out.println(" > Inicio > Minhas inscricoes > Lista de cursos > " + curso.getNome());
        System.out.println("");
        System.out.println(" CODIGO......: " + curso.getCodigoCompartilhavel());
        System.out.println(" CURSO.......: " + curso.getNome());
        System.out.println(" AUTOR.......: " + nomeAutor);
        System.out.println(" DESCRICAO...: " + curso.getDescricao());
        System.out.println(" DATA INICIO.: " + curso.getDataInicio().format(FORMATO_DATA));
        System.out.println("");
        System.out.println("----------------------------------------");
        if (curso.getEstado() == Curso.ATIVO_INSCRICOES) {
            System.out.println(" (A) Fazer minha inscricao no curso");
        } else {
            System.out.println(" " + obterEstadoCurso(curso.getEstado()));
        }
        System.out.println(" (R) Retornar ao menu anterior");
        System.out.print("Opcao: ");
    }

    /**
     * Mostra as inscricoes do usuario
     */
    public void mostrarMinhasInscricoes(Curso[] cursos) {
        if (cursos == null || cursos.length == 0) {
            System.out.println("\n       EntrePares 1.0                  ");
            System.out.println("========================================");
            System.out.println(" > Inicio > Minhas inscricoes          ");
            System.out.println("\n Voce nao tem inscricoes.");
            System.out.println("\n (R) Retornar");
            System.out.print("Opcao: ");
            return;
        }

        System.out.println("\n       EntrePares 1.0                  ");
        System.out.println("========================================");
        System.out.println(" > Inicio > Minhas inscricoes          ");
        System.out.println("");

        for (int i = 0; i < Math.min(cursos.length, 10); i++) {
            String estado = obterEstadoCurso(cursos[i].getEstado());
            System.out.println("(" + (i + 1) + ") " + cursos[i].getNome() + " - " + cursos[i].getDataInicio().format(FORMATO_DATA) + " " + estado);
        }

        System.out.println("");
        System.out.println("----------------------------------------");
        System.out.println(" (R) Retornar");
        System.out.print("Opcao: ");
    }

    /**
     * Mostra detalhes da inscricao do usuario
     */
    public void mostrarDetalheInscricao(Curso curso, String nomeAutor) {
        System.out.println("\n       EntrePares 1.0                  ");
        System.out.println("========================================");
        System.out.println(" > Inicio > Minhas inscricoes > " + curso.getNome());
        System.out.println("");
        System.out.println(" CODIGO......: " + curso.getCodigoCompartilhavel());
        System.out.println(" CURSO.......: " + curso.getNome());
        System.out.println(" AUTOR.......: " + nomeAutor);
        System.out.println(" DESCRICAO...: " + curso.getDescricao());
        System.out.println(" DATA INICIO.: " + curso.getDataInicio().format(FORMATO_DATA));
        System.out.println("");
        System.out.println("----------------------------------------");
        System.out.println(" (A) Cancelar minha inscricao no curso");
        System.out.println(" (R) Retornar ao menu anterior");
        System.out.print("Opcao: ");
    }

    /**
     * Pede o codigo de um curso para busca
     */
    public String pegarCodigoCurso() {
        System.out.print("Digite o codigo do curso: ");
        return scanner.nextLine().trim();
    }

    /**
     * Pega palavras de busca
     */
    public String pegarPalavrasBusca() {
        System.out.print("Digite as palavras-chave do curso: ");
        return scanner.nextLine().trim();
    }

    public char pegarOpcao() {
        String entrada = scanner.nextLine().toUpperCase().trim();
        if (entrada.length() > 0) {
            return entrada.charAt(0);
        }
        return ' ';
    }

    /**
     * Pega numero para selecao de item
     */
    public int pegarNumero() {
        try {
            String entrada = scanner.nextLine().trim();
            return Integer.parseInt(entrada);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Retorna a pagina atual
     */
    public int getPaginaAtual() {
        return paginaAtual;
    }

    /**
     * Avanca para a proxima pagina
     */
    public void proximaPagina() {
        if (paginaAtual < totalPaginas) {
            paginaAtual++;
        }
    }

    /**
     * Volta para a pagina anterior
     */
    public void paginaAnterior() {
        if (paginaAtual > 1) {
            paginaAtual--;
        }
    }

    /**
     * Reseta o indice de pagina
     */
    public void resetarPagina() {
        paginaAtual = 1;
    }

    public void mostrarMensagemInscricaoRealizada() {
        System.out.println("\n Inscricao realizada com sucesso!");
    }

    public void mostrarMensagemJaInscrito() {
        System.out.println("\n Voce ja esta inscrito neste curso.");
    }

    public void mostrarMensagemInscricoesEncerradas() {
        System.out.println("\n As inscricoes para este curso foram encerradas.");
    }

    public void mostrarMensagemCursoNaoEncontrado() {
        System.out.println("\n Curso nao encontrado.");
    }

    public void mostrarMensagemCancelamentoRealizado() {
        System.out.println("\n Inscricao cancelada com sucesso!");
    }

    public void mostrarMensagemErro(String mensagem) {
        System.out.println("\n Erro: " + mensagem);
    }

    public void mostrarMensagemOpcaoInvalida() {
        System.out.println("\n Opcao invalida!");
    }

    public void mostrarMensagemFuncionalidadeEmDesenvolvimento() {
        System.out.println("\n Funcionalidade em desenvolvimento (TP3).");
    }
}
