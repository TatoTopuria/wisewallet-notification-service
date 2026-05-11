package com.wisewallet.notification.infrastructure.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ThymeleafTemplateRenderer {

    private final TemplateEngine templateEngine;

    /**
     * Renders a Thymeleaf template from the classpath (templates/ prefix is automatic).
     *
     * @param templateName e.g. "email/balance-low"
     * @param variables    variables to inject into the template
     * @return rendered HTML string
     */
    public String render(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }
}
