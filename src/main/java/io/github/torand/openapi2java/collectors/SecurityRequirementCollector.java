/*
 * Copyright (c) 2024-2025 Tore Eide Andersen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.torand.openapi2java.collectors;

import io.github.torand.openapi2java.generators.Options;
import io.github.torand.openapi2java.model.AnnotationInfo;
import io.github.torand.openapi2java.model.SecurityRequirementInfo;
import io.swagger.v3.oas.models.security.SecurityRequirement;

import java.util.ArrayList;
import java.util.List;

import static io.github.torand.javacommons.collection.CollectionHelper.nonEmpty;
import static io.github.torand.javacommons.lang.StringHelper.quoteAll;
import static io.github.torand.openapi2java.utils.StringUtils.joinCsv;

/**
 * Collects information about a security requirement from a collection of OpenAPI security requirements.
 */
public class SecurityRequirementCollector extends BaseCollector {

    public SecurityRequirementCollector(Options opts) {
        super(opts);
    }

    public SecurityRequirementInfo getSequrityRequirementInfo(List<SecurityRequirement> securityRequirements) {
        if (securityRequirements.size() > 1) {
            throw new IllegalStateException("Multiple alternative security requirements not supported");
        }

        SecurityRequirement securityRequirement = securityRequirements.get(0);
        if (securityRequirement.size() > 1) {
            throw new IllegalStateException("Multiple mandatory security scheme names not supported");
        }

        String scheme = securityRequirement.keySet().iterator().next();

        SecurityRequirementInfo secReqInfo = new SecurityRequirementInfo(scheme)
            .withScopes(securityRequirement.get(scheme));

        if (opts.addMpOpenApiAnnotations()) {
            List<String> params = new ArrayList<>();
            params.add("name = \"%s\"".formatted(scheme));
            if (nonEmpty(secReqInfo.scopes())) {
                params.add("scopes = %s".formatted(formatAnnotationNamedParam(quoteAll(secReqInfo.scopes()))));
            }

            secReqInfo = secReqInfo.withAnnotation(
                new AnnotationInfo("@SecurityRequirement(%s)".formatted(joinCsv(params)), "org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement")
            );
        }

        return secReqInfo;
    }
}
