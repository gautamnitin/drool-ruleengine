package com.drool.ruleengine.service;

import com.drool.ruleengine.model.Rule;
import com.drool.ruleengine.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RuleEngineService {
    private final RuleRepository ruleRepo;

    public String generateDRL(List<Rule> rules) {
        StringBuilder drl = new StringBuilder();
        drl.append("package com.rules;\n");
        drl.append("import com.drool.ruleengine.model.Transaction;\n");
        for (Rule r : rules) {
            if (r.isActive()) {
                drl.append("rule \"" + r.getCustomerId() + "-" + r.getName() + "\"\n")
                        .append("    salience " + r.getPriority() + "\n")
                        .append("    when\n")
                        .append("        t : Transaction(" + r.getDrlCondition() + ")\n")
                        .append("    then\n")
                        .append("        t.setCategory(\"" + r.getCategory().getName() + "\");\n")
                        .append("        update(t);\n")
                        .append("end\n\n");
            }
        }
        return drl.toString();
    }

    // Builds and returns a Drools KieSession from the generated DRL
    public KieSession getSessionWithRules(String customerId) {
        List<Rule> rules = ruleRepo.findByCustomerId(customerId); // Assumes Category is eagerly fetched or joined
        String drlContent = generateDRL(rules);
        System.out.println(">>>>Generated DRL:\n" + drlContent);
        KieHelper kieHelper = new KieHelper();
        kieHelper.addContent(drlContent, ResourceType.DRL);

        return kieHelper.build().newKieSession();
    }



    /*public KieSession getSessionWithRules() {
        String drl = generateDRL(ruleRepo.findAll());
        System.out.println(">>>>Generated DRL:\n" + drl);
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.write("src/main/resources/rules.drl", ks.getResources()
                .newByteArrayResource(drl.getBytes(StandardCharsets.UTF_8)));

        KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
        KieContainer kc = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
        return kc.newKieSession();
    }*/
}
