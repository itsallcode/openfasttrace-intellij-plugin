package org.itsallcode.openfasttrace.intellijplugin.trace;

import org.itsallcode.openfasttrace.api.core.ItemStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

class OftTraceInputsTest {
    @Test
    void givenWholeProjectInputsWhenCreatedThenPropertiesMatch() {
        final Path root = Path.of("/project/root");
        final OftTraceInputs inputs = OftTraceInputs.wholeProject(
                root,
                List.of("req", "dsn"),
                List.of("tag1"),
                true,
                Set.of(ItemStatus.APPROVED, ItemStatus.DRAFT)
        );

        Assertions.assertAll(
                () -> assertThat(inputs.isWholeProject(), is(true)),
                () -> assertThat(inputs.inputPaths(), contains(root)),
                () -> assertThat(inputs.artifactTypes(), contains("req", "dsn")),
                () -> assertThat(inputs.tags(), contains("tag1")),
                () -> assertThat(inputs.includeUntagged(), is(true)),
                () -> assertThat(inputs.selectedStatuses(), is(Set.of(ItemStatus.APPROVED, ItemStatus.DRAFT))),
                () -> assertThat(inputs.progressText(), is(root.toString()))
        );
    }

    @Test
    void givenWholeProjectConvenienceOverloadsWhenCreatedThenDefaultsApply() {
        final Path root = Path.of("/project/root");
        final OftTraceInputs defaultInputs = OftTraceInputs.wholeProject(root, List.of(), List.of());
        final OftTraceInputs untaggedInputs = OftTraceInputs.wholeProject(root, List.of(), List.of(), true);
        final OftTraceInputs statusInputs = OftTraceInputs.wholeProject(root, List.of(), List.of(), Set.of(ItemStatus.PROPOSED));

        Assertions.assertAll(
                () -> assertThat(defaultInputs.includeUntagged(), is(false)),
                () -> assertThat(defaultInputs.selectedStatuses(), is(OftTraceSettingsSnapshot.DEFAULT_STATUSES)),
                () -> assertThat(untaggedInputs.includeUntagged(), is(true)),
                () -> assertThat(untaggedInputs.selectedStatuses(), is(OftTraceSettingsSnapshot.DEFAULT_STATUSES)),
                () -> assertThat(statusInputs.includeUntagged(), is(false)),
                () -> assertThat(statusInputs.selectedStatuses(), is(Set.of(ItemStatus.PROPOSED)))
        );
    }

    @Test
    void givenSelectedResourcesInputsWhenCreatedThenPropertiesMatch() {
        final List<Path> paths = List.of(Path.of("/project/doc"), Path.of("/project/src"));
        final OftTraceInputs inputs = OftTraceInputs.selectedResources(
                paths,
                List.of("impl"),
                List.of("tag2"),
                true,
                Set.of(ItemStatus.REJECTED)
        );

        Assertions.assertAll(
                () -> assertThat(inputs.isWholeProject(), is(false)),
                () -> assertThat(inputs.inputPaths(), is(paths)),
                () -> assertThat(inputs.artifactTypes(), contains("impl")),
                () -> assertThat(inputs.tags(), contains("tag2")),
                () -> assertThat(inputs.includeUntagged(), is(true)),
                () -> assertThat(inputs.selectedStatuses(), is(Set.of(ItemStatus.REJECTED))),
                () -> assertThat(inputs.progressText(), is("2 configured trace input(s)"))
        );
    }

    @Test
    void givenSelectedResourcesConvenienceOverloadsWhenCreatedThenDefaultsApply() {
        final List<Path> paths = List.of(Path.of("/project/doc"));
        final OftTraceInputs defaultInputs = OftTraceInputs.selectedResources(paths, List.of(), List.of());
        final OftTraceInputs untaggedInputs = OftTraceInputs.selectedResources(paths, List.of(), List.of(), true);
        final OftTraceInputs statusInputs = OftTraceInputs.selectedResources(paths, List.of(), List.of(), Set.of(ItemStatus.DRAFT));

        Assertions.assertAll(
                () -> assertThat(defaultInputs.includeUntagged(), is(false)),
                () -> assertThat(defaultInputs.selectedStatuses(), is(OftTraceSettingsSnapshot.DEFAULT_STATUSES)),
                () -> assertThat(untaggedInputs.includeUntagged(), is(true)),
                () -> assertThat(untaggedInputs.selectedStatuses(), is(OftTraceSettingsSnapshot.DEFAULT_STATUSES)),
                () -> assertThat(statusInputs.includeUntagged(), is(false)),
                () -> assertThat(statusInputs.selectedStatuses(), is(Set.of(ItemStatus.DRAFT)))
        );
    }

    @Test
    void givenEmptyStatusesWhenCreatingInputsThenItThrowsException() {
        final IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> OftTraceInputs.wholeProject(Path.of("."), List.of(), List.of(), Set.of())
        );

        assertThat(exception.getMessage(), is("At least one specification item status must be selected."));
    }
}
