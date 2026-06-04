package TP03.storage;

import TP03.models.*;
import java.text.Normalizer;
import java.util.ArrayList;

public class ArquivoCurso extends Arquivo<Curso> {

    HashExtensivel<ParCodigoID> indiceIndiretoCodigo;
    ArvoreBMais<ParUsuarioCurso> arvoreBMais;
    ListaInvertida indiceInvertidoNome;

    private static final String[] STOP_WORDS = {
            "a", "as", "o", "os", "um", "uma", "uns", "umas",
            "de", "da", "das", "do", "dos", "em", "no", "nos", "na", "nas",
            "por", "para", "com", "sem", "sob", "sobre", "entre", "ate",
            "e", "ou", "mas", "que", "se", "ao", "aos",
            "pela", "pelas", "pelo", "pelos", "num", "numa", "nuns", "numas",
            "meu", "minha", "seu", "sua", "seus", "suas",
            "i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x"
    };

    public ArquivoCurso() throws Exception {
        super("cursos", Curso.class.getConstructor());

        indiceIndiretoCodigo = new HashExtensivel<>(
                ParCodigoID.class.getConstructor(),
                4,
                DIRETORIO_DADOS + "\\cursos\\indiceCodigo.d.db",
                DIRETORIO_DADOS + "\\cursos\\indiceCodigo.c.db");
        arvoreBMais = new ArvoreBMais<>(ParUsuarioCurso.class.getConstructor(), 8,
                DIRETORIO_DADOS + "\\cursos\\arvoreCursos.db");
        indiceInvertidoNome = new ListaInvertida(
                10,
                DIRETORIO_DADOS + "\\cursos\\indiceNome.dic.db",
                DIRETORIO_DADOS + "\\cursos\\indiceNome.blocos.db");

        reconstruirIndiceInvertidoSeNecessario();
    }

    @Override
    public int create(Curso c) throws Exception {
        int id = super.create(c);

        ParCodigoID parCodigo = new ParCodigoID(c.getCodigoCompartilhavel(), id);
        indiceIndiretoCodigo.create(parCodigo);

        ParUsuarioCurso par = new ParUsuarioCurso(c.getIdUsuario(), id);
        boolean inserido = arvoreBMais.create(par);

        indiceInvertidoNome.incrementaEntidades();
        indexarNomeCurso(c);

        System.out.println("DEBUG: Curso criado. ID=" + id + ", IdUsuario=" + c.getIdUsuario()
                + ", Inserido na arvore=" + inserido);
        return id;
    }

    public ArrayList<Curso> readByUser(int idUsuario) throws Exception {
        ArrayList<Curso> cursos = new ArrayList<>();

        try {
            ArrayList<ParUsuarioCurso> pares = arvoreBMais.read(null);

            if (pares == null || pares.isEmpty()) {
                return cursos;
            }

            ArrayList<ParUsuarioCurso> filteredPares = new ArrayList<>();
            for (ParUsuarioCurso p : pares) {
                if (p.getIdUsuario() == idUsuario) {
                    filteredPares.add(p);
                }
            }

            for (ParUsuarioCurso par : filteredPares) {
                Curso curso = super.read(par.getIdCurso());
                if (curso != null) {
                    cursos.add(curso);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return cursos;
    }

    @Override
    public Curso read(int id) throws Exception {
        return super.read(id);
    }

    public Curso read(String codigoCompartilhavel) throws Exception {
        ParCodigoID parCodigo = indiceIndiretoCodigo.read(ParCodigoID.hash(codigoCompartilhavel));

        if (parCodigo != null) {
            int idCurso = parCodigo.getId();
            return super.read(idCurso);
        }

        return null;
    }

    public Curso[] readPorPalavras(String consulta) throws Exception {
        ArrayList<String> termosBusca = termosUnicos(consulta);
        ArrayList<ElementoLista> pontuacoes = new ArrayList<>();
        int totalCursos = indiceInvertidoNome.numeroEntidades();

        if (totalCursos <= 0 || termosBusca.isEmpty()) {
            return new Curso[0];
        }

        for (String termo : termosBusca) {
            ElementoLista[] lista = indiceInvertidoNome.read(termo);
            if (lista.length == 0) {
                continue;
            }

            float idf = (float) (Math.log10((double) totalCursos / lista.length) + 1);

            for (ElementoLista elemento : lista) {
                float pontuacao = elemento.getFrequencia() * idf;
                int posicao = posicaoPorId(pontuacoes, elemento.getId());

                if (posicao >= 0) {
                    ElementoLista atual = pontuacoes.get(posicao);
                    atual.setFrequencia(atual.getFrequencia() + pontuacao);
                } else {
                    pontuacoes.add(new ElementoLista(elemento.getId(), pontuacao));
                }
            }
        }

        ordenarPorPontuacaoDecrescente(pontuacoes);

        ArrayList<Curso> cursos = new ArrayList<>();
        for (ElementoLista pontuacao : pontuacoes) {
            Curso curso = super.read(pontuacao.getId());
            if (curso != null) {
                cursos.add(curso);
            }
        }

        return cursos.toArray(new Curso[0]);
    }

    @Override
    public boolean delete(int id) throws Exception {
        Curso c = super.read(id);
        if (c != null) {
            ParUsuarioCurso par = new ParUsuarioCurso(c.getIdUsuario(), id);
            arvoreBMais.delete(par);

            if (super.delete(id)) {
                removerNomeCursoDoIndice(c);
                indiceInvertidoNome.decrementaEntidades();
                return indiceIndiretoCodigo.delete(ParCodigoID.hash(c.getCodigoCompartilhavel()));
            }
        }
        return false;
    }

    @Override
    public boolean update(Curso novoCurso) throws Exception {
        Curso cursoVelho = super.read(novoCurso.getId());

        if (super.update(novoCurso)) {
            if (cursoVelho != null && !normalizarTexto(cursoVelho.getNome()).equals(normalizarTexto(novoCurso.getNome()))) {
                removerNomeCursoDoIndice(cursoVelho);
                indexarNomeCurso(novoCurso);
            }

            if (cursoVelho != null && novoCurso.getIdUsuario() != cursoVelho.getIdUsuario()) {
                ParUsuarioCurso parVelho = new ParUsuarioCurso(cursoVelho.getIdUsuario(), novoCurso.getId());
                arvoreBMais.delete(parVelho);

                ParUsuarioCurso parNovo = new ParUsuarioCurso(novoCurso.getIdUsuario(), novoCurso.getId());
                arvoreBMais.create(parNovo);
            }
            return true;
        }
        return false;
    }

    private void indexarNomeCurso(Curso curso) throws Exception {
        ArrayList<String> termos = separarTermos(curso.getNome());
        ArrayList<String> unicos = termosUnicos(termos);

        if (termos.isEmpty()) {
            return;
        }

        for (String termo : unicos) {
            int ocorrencias = contarOcorrencias(termos, termo);
            float tf = (float) ocorrencias / termos.size();
            indiceInvertidoNome.create(termo, new ElementoLista(curso.getId(), tf));
        }
    }

    private void removerNomeCursoDoIndice(Curso curso) throws Exception {
        ArrayList<String> termos = termosUnicos(curso.getNome());

        for (String termo : termos) {
            indiceInvertidoNome.delete(termo, curso.getId());
        }
    }

    private void reconstruirIndiceInvertidoSeNecessario() throws Exception {
        if (indiceInvertidoNome.numeroEntidades() > 0 || ultimoIdRegistrado() == 0) {
            return;
        }

        int ultimoId = ultimoIdRegistrado();
        for (int id = 1; id <= ultimoId; id++) {
            Curso curso = super.read(id);
            if (curso != null) {
                indiceInvertidoNome.incrementaEntidades();
                indexarNomeCurso(curso);
            }
        }
    }

    private int ultimoIdRegistrado() throws Exception {
        arquivo.seek(0);
        return arquivo.readInt();
    }

    private ArrayList<String> separarTermos(String texto) {
        ArrayList<String> termos = new ArrayList<>();
        String normalizado = normalizarTexto(texto);
        String[] palavras = normalizado.split("[^a-z0-9]+");

        for (String palavra : palavras) {
            if (palavra.length() > 0 && !ehNumero(palavra) && !ehStopWord(palavra)) {
                termos.add(palavra);
            }
        }

        return termos;
    }

    private ArrayList<String> termosUnicos(String texto) {
        return termosUnicos(separarTermos(texto));
    }

    private ArrayList<String> termosUnicos(ArrayList<String> termos) {
        ArrayList<String> unicos = new ArrayList<>();

        for (String termo : termos) {
            if (posicaoTermo(unicos, termo) < 0) {
                unicos.add(termo);
            }
        }

        return unicos;
    }

    private int contarOcorrencias(ArrayList<String> termos, String termoBuscado) {
        int ocorrencias = 0;

        for (String termo : termos) {
            if (termo.equals(termoBuscado)) {
                ocorrencias++;
            }
        }

        return ocorrencias;
    }

    private int posicaoTermo(ArrayList<String> termos, String termoBuscado) {
        for (int i = 0; i < termos.size(); i++) {
            if (termos.get(i).equals(termoBuscado)) {
                return i;
            }
        }

        return -1;
    }

    private int posicaoPorId(ArrayList<ElementoLista> elementos, int id) {
        for (int i = 0; i < elementos.size(); i++) {
            if (elementos.get(i).getId() == id) {
                return i;
            }
        }

        return -1;
    }

    private void ordenarPorPontuacaoDecrescente(ArrayList<ElementoLista> elementos) {
        for (int i = 0; i < elementos.size() - 1; i++) {
            for (int j = 0; j < elementos.size() - i - 1; j++) {
                ElementoLista atual = elementos.get(j);
                ElementoLista proximo = elementos.get(j + 1);

                if (atual.getFrequencia() < proximo.getFrequencia()) {
                    elementos.set(j, proximo);
                    elementos.set(j + 1, atual);
                }
            }
        }
    }

    private String normalizarTexto(String texto) {
        String semAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase();
    }

    private boolean ehNumero(String palavra) {
        for (int i = 0; i < palavra.length(); i++) {
            if (!Character.isDigit(palavra.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private boolean ehStopWord(String palavra) {
        for (String stopWord : STOP_WORDS) {
            if (stopWord.equals(palavra)) {
                return true;
            }
        }

        return false;
    }
}
