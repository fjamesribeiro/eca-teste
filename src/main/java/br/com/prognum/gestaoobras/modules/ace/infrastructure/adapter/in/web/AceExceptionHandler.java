package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.in.web;

import br.com.prognum.gestaoobras.modules.ace.domain.exception.AceException;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.CodigoTotpInvalidoException;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.SenhaAtualIncorretaException;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.SenhasDiferentesException;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.SessaoNaoEncontradaException;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.StatusInvalidoException;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.TokenInvalidoException;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.UsuarioNaoEncontradoException;
import br.com.prognum.gestaoobras.modules.ace.domain.service.PoliticaSenhaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/**
 * Handler global de exceções do módulo ACE.
 * Retorna respostas RFC 7807 (Problem Details).
 */
@RestControllerAdvice(basePackages = "br.com.prognum.gestaoobras.modules.ace")
public class AceExceptionHandler {

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ProblemDetail handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException ex) {
        return buildProblem(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(SessaoNaoEncontradaException.class)
    public ProblemDetail handleSessaoNaoEncontrada(SessaoNaoEncontradaException ex) {
        return buildProblem(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler({
            TokenInvalidoException.class,
            SenhasDiferentesException.class,
            StatusInvalidoException.class,
            CodigoTotpInvalidoException.class,
            SenhaAtualIncorretaException.class
    })
    public ProblemDetail handleBadRequest(AceException ex) {
        return buildProblem(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(PoliticaSenhaService.SenhaInvalidaException.class)
    public ProblemDetail handleSenhaInvalida(PoliticaSenhaService.SenhaInvalidaException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Senha inválida");
        problem.setType(URI.create("urn:ace:error:V-ACE-03"));
        problem.setProperty("codigo", "V-ACE-03");
        return problem;
    }

    @ExceptionHandler(PoliticaSenhaService.SenhaReutilizadaException.class)
    public ProblemDetail handleSenhaReutilizada(PoliticaSenhaService.SenhaReutilizadaException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Senha reutilizada");
        problem.setType(URI.create("urn:ace:error:SENHA_REUTILIZADA"));
        problem.setProperty("codigo", "SENHA_REUTILIZADA");
        return problem;
    }

    private ProblemDetail buildProblem(HttpStatus status, AceException ex) {
        var problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problem.setTitle(ex.getCodigoErro());
        problem.setType(URI.create("urn:ace:error:" + ex.getCodigoErro()));
        problem.setProperty("codigo", ex.getCodigoErro());
        return problem;
    }
}
