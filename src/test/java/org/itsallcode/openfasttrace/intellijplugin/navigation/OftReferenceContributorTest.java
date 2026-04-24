package org.itsallcode.openfasttrace.intellijplugin.navigation;

import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import org.itsallcode.openfasttrace.intellijplugin.AbstractOftPlatformTestCase;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class OftReferenceContributorTest extends AbstractOftPlatformTestCase {
    public void testGivenCoverageTagReferenceContributorWhenRegisteringThenItProvidesAReferenceProvider() {
        final CapturingReferenceRegistrar registrar = new CapturingReferenceRegistrar();

        new OftCoverageTagReferenceContributor().registerReferenceProviders(registrar);

        assertThat(registrar.getProvider(), notNullValue());
    }

    public void testGivenSpecificationReferenceContributorWhenRegisteringThenItProvidesAReferenceProvider() {
        final CapturingReferenceRegistrar registrar = new CapturingReferenceRegistrar();

        new OftSpecificationReferenceContributor().registerReferenceProviders(registrar);

        assertThat(registrar.getProvider(), notNullValue());
    }

    public void testGivenCoverageTagFileWhenGettingReferencesThenItResolvesSourceAndTargetDeclarations() {
        myFixture.addFileToProject("doc/spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: dsn
                """);
        myFixture.addFileToProject("doc/impl.md", """
                impl~openfasttrace_navigation_target~1
                Covers:
                - req~openfasttrace_navigation_target~1
                """);
        final String coverageTag = "[impl" + "->req~openfasttrace_navigation_target~1]";
        myFixture.configureByText("Main.java", "// " + coverageTag + "\n");

        final PsiReferenceProvider provider = coverageTagReferenceProvider();
        final PsiReference[] references =
                provider.getReferencesByElement(requiredElementAt(3), new ProcessingContext());
        final List<String> resolvedFiles = Arrays.stream(references)
                .map(reference -> ((PsiPolyVariantReference) reference).multiResolve(false))
                .flatMap(Arrays::stream)
                .map(result -> Objects.requireNonNull(result.getElement()).getContainingFile().getName())
                .toList();

        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(references.length, is(2)),
                () -> assertThat(references[0], instanceOf(OftCoverageTagReference.class)),
                () -> assertThat(resolvedFiles, contains("impl.md", "spec.md"))
        );
    }

    public void testGivenNonCoverageTagFileWhenGettingReferencesThenItReturnsNoReferences() {
        myFixture.configureByText("notes.txt", "req~openfasttrace_navigation_target~1");

        final PsiReference[] references =
                coverageTagReferenceProvider().getReferencesByElement(requiredElementAt(0), new ProcessingContext());

        assertThat(references.length, is(0));
    }

    public void testGivenSpecificationFileWhenGettingReferencesThenItResolvesCoveredDeclarations() {
        myFixture.addFileToProject("doc/spec.md", """
                req~openfasttrace_navigation_target~1
                Needs: dsn
                """);
        myFixture.configureByText("design.md", """
                dsn~openfasttrace_navigation_design~1
                Covers:
                - req~openfasttrace_navigation_target~1
                """);

        final PsiReferenceProvider provider = specificationReferenceProvider();
        final PsiReference[] references =
                provider.getReferencesByElement(requiredElementAt(50), new ProcessingContext());
        final List<String> resolvedFiles = Arrays.stream(references)
                .map(reference -> ((PsiPolyVariantReference) reference).multiResolve(false))
                .flatMap(Arrays::stream)
                .map(result -> Objects.requireNonNull(result.getElement()).getContainingFile().getName())
                .toList();

        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(references.length, is(1)),
                () -> assertThat(references[0], instanceOf(OftCoverageTagReference.class)),
                () -> assertThat(resolvedFiles, contains("spec.md"))
        );
    }

    public void testGivenNonSpecificationFileWhenGettingReferencesThenItReturnsNoReferences() {
        final String coverageTag = "[impl" + "->req~openfasttrace_navigation_target~1]";
        myFixture.configureByText("Main.java", "// " + coverageTag + "\n");

        final PsiReference[] references =
                specificationReferenceProvider().getReferencesByElement(requiredElementAt(3), new ProcessingContext());

        assertThat(references.length, is(0));
    }

    private PsiReferenceProvider coverageTagReferenceProvider() {
        final CapturingReferenceRegistrar registrar = new CapturingReferenceRegistrar();
        new OftCoverageTagReferenceContributor().registerReferenceProviders(registrar);
        return registrar.getProvider();
    }

    private PsiReferenceProvider specificationReferenceProvider() {
        final CapturingReferenceRegistrar registrar = new CapturingReferenceRegistrar();
        new OftSpecificationReferenceContributor().registerReferenceProviders(registrar);
        return registrar.getProvider();
    }

    private PsiElement requiredElementAt(final int offset) {
        final PsiElement element = myFixture.getFile().findElementAt(offset);
        if (element == null) {
            org.junit.jupiter.api.Assertions.fail("Expected PSI element at offset " + offset);
        }
        return element;
    }

    private static final class CapturingReferenceRegistrar extends PsiReferenceRegistrar {
        private PsiReferenceProvider provider;

        @Override
        public <T extends PsiElement> void registerReferenceProvider(
                final @NonNull ElementPattern<T> pattern,
                final @NonNull PsiReferenceProvider provider,
                final double priority
        ) {
            this.provider = provider;
        }

        private PsiReferenceProvider getProvider() {
            return provider;
        }
    }
}
