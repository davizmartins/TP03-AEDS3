package TP03;

import TP03.models.*;
import TP03.storage.*;
import TP03.controllers.*;
import TP03.views.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Instanciar os componentes
            VisaoUsuario visaoUsuario = new VisaoUsuario();
            ArquivoUsuario arquivoUsuario = new ArquivoUsuario();
            
            VisaoCurso visaoCurso = new VisaoCurso();
            ArquivoCurso arquivoCurso = new ArquivoCurso();
            ControleCurso controleCurso = new ControleCurso(visaoCurso, arquivoCurso);
            
            // Componentes de Inscrição (TP03)
            VisaoInscricao visaoInscricao = new VisaoInscricao();
            ArquivoInscricao arquivoInscricao = new ArquivoInscricao();
            arquivoInscricao.setArquivoUsuario(arquivoUsuario); // Injetar dependência
            
            ControleInscricao controleInscricao = new ControleInscricao(visaoInscricao, arquivoInscricao, arquivoCurso, arquivoUsuario);
            controleCurso.setControleInscricao(controleInscricao); // Injetar dependência
            
            // Controle de usuários
            ControleUsuario controleUsuario = new ControleUsuario(visaoUsuario, arquivoUsuario, controleCurso, controleInscricao);

            // Iniciar o sistema
            controleUsuario.mostrarMenu();

        } catch (Exception e) {
            System.out.println("✗ Erro crítico ao inicializar o sistema: " + e.getMessage());
            e.printStackTrace();
        }
    }
}