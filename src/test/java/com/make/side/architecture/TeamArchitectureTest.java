package com.make.side.architecture;

import com.make.side.entity.Team;
import com.make.side.entity.TeamFactory;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.constructors;

class TeamArchitectureTest {

    private static final JavaClasses classes = new ClassFileImporter().importPackages("com.make.side");

    /**
     * Team의 생성자는 오직 TeamFactory에서만 호출되어야 한다.
     */
    @Test
    void team_constructor_should_only_be_called_by_team_factory() {
        ArchRule rule = constructors()
                .that().areDeclaredIn(Team.class)
                .should(new ArchCondition<>("be called only from TeamFactory") {
                    @Override
                    public void check(JavaConstructor constructor, ConditionEvents events) {
                        for (JavaConstructorCall call : constructor.getCallsOfSelf()) {
                            if (!call.getOriginOwner().isEquivalentTo(TeamFactory.class)) {
                                events.add(SimpleConditionEvent.violated(
                                        call,
                                        call.getDescription() + " is not from TeamFactory (origin: "
                                                + call.getOriginOwner().getName() + ")"));
                            }
                        }
                    }
                });
        rule.check(classes);
    }
}
