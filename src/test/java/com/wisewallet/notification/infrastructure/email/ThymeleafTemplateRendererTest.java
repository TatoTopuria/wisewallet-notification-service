package com.wisewallet.notification.infrastructure.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThymeleafTemplateRendererTest {

    @Mock
    TemplateEngine templateEngine;

    @InjectMocks
    ThymeleafTemplateRenderer renderer;

    @Test
    void render_passesTemplateNameAndVariablesToEngine() {
        String templateName = "email/account-created";
        Map<String, Object> vars = Map.of("firstName", "Alice");
        String expectedHtml = "<html>Hello Alice</html>";

        when(templateEngine.process(eq(templateName), any(Context.class))).thenReturn(expectedHtml);

        String result = renderer.render(templateName, vars);

        assertThat(result).isEqualTo(expectedHtml);
        verify(templateEngine).process(eq(templateName), any(Context.class));
    }

    @Test
    void render_emptyVariables_delegatesToEngine() {
        when(templateEngine.process(any(String.class), any(Context.class))).thenReturn("<html/>");

        String result = renderer.render("email/balance-low", Map.of());

        assertThat(result).isEqualTo("<html/>");
    }
}
