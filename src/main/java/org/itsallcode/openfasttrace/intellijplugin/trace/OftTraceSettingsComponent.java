package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.nio.file.Path;

public final class OftTraceSettingsComponent {
    private final JBRadioButton wholeProjectRadioButton =
            new JBRadioButton("Trace the whole project");
    private final JBRadioButton selectedResourcesRadioButton =
            new JBRadioButton("Trace selected resources");
    private final JBCheckBox includeSourceRootsCheckBox =
            new JBCheckBox("Include IntelliJ source directories");
    private final JBCheckBox includeTestRootsCheckBox =
            new JBCheckBox("Include IntelliJ test directories");
    private final JBTextArea additionalPathsTextArea = new JBTextArea();
    private final JBTextField artifactTypesField = new JBTextField();
    private final JBTextField tagsField = new JBTextField();
    private final JBCheckBox includeUntaggedCheckBox =
            new JBCheckBox("Include untagged items");
    private final JBCheckBox showTransitiveDefectsCheckBox =
            new JBCheckBox("Show transitive defects");
    private final JBRadioButton plainTextResultViewRadioButton =
            new JBRadioButton("Plain text output");
    private final JBRadioButton testRunnerResultViewRadioButton =
            new JBRadioButton("IntelliJ test runner UI");
    private final JBLabel resolvedRelativeToLabel = new JBLabel();
    private final JBTextArea validationMessagesArea = new JBTextArea();
    private final Path projectRoot;
    private final boolean showResultViewSelection;
    private final JPanel panel;

    public OftTraceSettingsComponent(final Path projectRoot, final boolean showResultViewSelection) {
        this.projectRoot = projectRoot;
        this.showResultViewSelection = showResultViewSelection;
        final ButtonGroup traceScopeGroup = new ButtonGroup();
        traceScopeGroup.add(wholeProjectRadioButton);
        traceScopeGroup.add(selectedResourcesRadioButton);
        final ButtonGroup resultViewGroup = new ButtonGroup();
        resultViewGroup.add(plainTextResultViewRadioButton);
        resultViewGroup.add(testRunnerResultViewRadioButton);
        selectedResourcesRadioButton.addActionListener(event -> updateSelectedResourcesEnabledState());
        wholeProjectRadioButton.addActionListener(event -> updateSelectedResourcesEnabledState());
        additionalPathsTextArea.setLineWrap(false);
        additionalPathsTextArea.setRows(5);
        additionalPathsTextArea.setColumns(40);
        additionalPathsTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent event) {
                updateValidationFeedback();
            }

            @Override
            public void removeUpdate(final DocumentEvent event) {
                updateValidationFeedback();
            }

            @Override
            public void changedUpdate(final DocumentEvent event) {
                updateValidationFeedback();
            }
        });
        validationMessagesArea.setEditable(false);
        validationMessagesArea.setOpaque(false);
        validationMessagesArea.setFocusable(false);
        validationMessagesArea.setLineWrap(true);
        validationMessagesArea.setWrapStyleWord(true);
        final JComponent scrollPane = new JBScrollPane(additionalPathsTextArea);
        scrollPane.setPreferredSize(new Dimension(420, 110));
        final JBPanel<?> bodyPanel = new JBPanel<>();
        bodyPanel.setLayout(new GridBagLayout());
        final GridBagConstraints bodyConstraints = new GridBagConstraints();
        bodyConstraints.gridx = 0;
        bodyConstraints.weightx = 1.0;
        bodyConstraints.fill = GridBagConstraints.HORIZONTAL;
        bodyConstraints.anchor = GridBagConstraints.WEST;
        bodyConstraints.insets = new Insets(0, 0, 0, 0);

        int row = 0;
        row = addSectionHeader(bodyPanel, bodyConstraints, row, "Trace Scope");
        row = addComponentRow(bodyPanel, bodyConstraints, row, wholeProjectRadioButton, 0, 0);
        row = addComponentRow(bodyPanel, bodyConstraints, row, selectedResourcesRadioButton, 4, 0);
        row = addComponentRow(bodyPanel, bodyConstraints, row, includeSourceRootsCheckBox, 4, 18);
        row = addComponentRow(bodyPanel, bodyConstraints, row, includeTestRootsCheckBox, 4, 18);
        row = addAdditionalPathsBlock(bodyPanel, bodyConstraints, row, scrollPane);

        row = addSectionSeparator(bodyPanel, bodyConstraints, row);
        row = addSectionHeader(bodyPanel, bodyConstraints, row, "Filters");
        row = addLabeledComponentRow(bodyPanel, bodyConstraints, row, "Artifact types:", artifactTypesField, 8);
        row = addHelpRow(bodyPanel, bodyConstraints, row, "comma-separated, empty = all");
        row = addLabeledComponentRow(bodyPanel, bodyConstraints, row, "Tags:", tagsField, 8);
        row = addHelpRow(bodyPanel, bodyConstraints, row, "comma-separated, empty = all");
        row = addIndentedComponentRow(bodyPanel, bodyConstraints, row, includeUntaggedCheckBox, 4);
        row = addLabeledComponentRow(bodyPanel, bodyConstraints, row, "Defects:", showTransitiveDefectsCheckBox, 4);
        if (showResultViewSelection) {
            row = addSectionSeparator(bodyPanel, bodyConstraints, row);
            row = addSectionHeader(bodyPanel, bodyConstraints, row, "Result view");
            row = addComponentRow(bodyPanel, bodyConstraints, row, plainTextResultViewRadioButton, 4, 18);
            addComponentRow(bodyPanel, bodyConstraints, row, testRunnerResultViewRadioButton, 4, 18);
        }

        panel = new JBPanel<>(new BorderLayout());
        panel.add(bodyPanel, BorderLayout.NORTH);
        setSettings(OftTraceSettingsSnapshot.DEFAULT);
    }

    public JComponent getPanel() {
        return panel;
    }

    public OftTraceSettingsSnapshot getSettings() {
        return new OftTraceSettingsSnapshot(
                selectedResourcesRadioButton.isSelected()
                        ? OftTraceScopeMode.SELECTED_RESOURCES
                        : OftTraceScopeMode.WHOLE_PROJECT,
                includeSourceRootsCheckBox.isSelected(),
                includeTestRootsCheckBox.isSelected(),
                additionalPathsTextArea.getText(),
                artifactTypesField.getText(),
                tagsField.getText(),
                includeUntaggedCheckBox.isSelected(),
                showTransitiveDefectsCheckBox.isSelected(),
                selectedResultView()
        );
    }

    public void setSettings(final OftTraceSettingsSnapshot settings) {
        wholeProjectRadioButton.setSelected(settings.scopeMode() == OftTraceScopeMode.WHOLE_PROJECT);
        selectedResourcesRadioButton.setSelected(settings.scopeMode() == OftTraceScopeMode.SELECTED_RESOURCES);
        includeSourceRootsCheckBox.setSelected(settings.includeSourceRoots());
        includeTestRootsCheckBox.setSelected(settings.includeTestRoots());
        additionalPathsTextArea.setText(settings.additionalPathsText());
        artifactTypesField.setText(settings.artifactTypesText());
        tagsField.setText(settings.tagsText());
        includeUntaggedCheckBox.setSelected(settings.includeUntagged());
        showTransitiveDefectsCheckBox.setSelected(settings.showTransitiveDefects());
        plainTextResultViewRadioButton.setSelected(settings.resultView() == OftTraceResultView.PLAIN_TEXT);
        testRunnerResultViewRadioButton.setSelected(settings.resultView() == OftTraceResultView.TEST_RUNNER);
        updateSelectedResourcesEnabledState();
    }

    public String validationMessagesText() {
        return validationMessagesArea.getText();
    }

    private void updateSelectedResourcesEnabledState() {
        final boolean enabled = selectedResourcesRadioButton.isSelected();
        includeSourceRootsCheckBox.setEnabled(enabled);
        includeTestRootsCheckBox.setEnabled(enabled);
        additionalPathsTextArea.setEnabled(enabled);
        resolvedRelativeToLabel.setEnabled(enabled);
        validationMessagesArea.setEnabled(enabled);
        updateValidationFeedback();
    }

    private OftTraceResultView selectedResultView() {
        if (!showResultViewSelection) {
            return OftTraceSettingsSnapshot.DEFAULT.resultView();
        }
        if (plainTextResultViewRadioButton.isSelected()) {
            return OftTraceResultView.PLAIN_TEXT;
        }
        return OftTraceResultView.TEST_RUNNER;
    }

    // [impl->dsn~show-per-line-validation-for-additional-trace-paths~1]
    private void updateValidationFeedback() {
        if (!selectedResourcesRadioButton.isSelected() || projectRoot == null) {
            resolvedRelativeToLabel.setText("");
            validationMessagesArea.setText("");
            resolvedRelativeToLabel.setVisible(false);
            validationMessagesArea.setVisible(false);
            return;
        }
        final OftAdditionalTracePathValidation validation =
                OftAdditionalTracePathValidation.validate(projectRoot, additionalPathsTextArea.getText());
        resolvedRelativeToLabel.setText(validation.resolvedRelativeToText());
        validationMessagesArea.setText(String.join(System.lineSeparator(), validation.messages()));
        resolvedRelativeToLabel.setVisible(!resolvedRelativeToLabel.getText().isEmpty());
        validationMessagesArea.setVisible(!validationMessagesArea.getText().isEmpty());
    }

    private static int addComponentRow(
            final JBPanel<?> panel,
            final GridBagConstraints constraints,
            final int row,
            final JComponent component,
            final int topInset,
            final int leftInset
    ) {
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1.0;
        constraints.insets = new Insets(topInset, leftInset, 0, 0);
        panel.add(component, constraints);
        return row + 1;
    }

    private int addAdditionalPathsBlock(
            final JBPanel<?> panel,
            final GridBagConstraints constraints,
            final int row,
            final JComponent scrollPane
    ) {
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1.0;
        constraints.insets = new Insets(8, 0, 0, 0);

        final JBPanel<?> additionalPathsPanel = new JBPanel<>(new BorderLayout(0, 4));
        additionalPathsPanel.add(
                new JBLabel("Additional project-relative files or directories (one per line)"),
                BorderLayout.NORTH
        );

        final JBPanel<?> validationPanel = new JBPanel<>(new BorderLayout(0, 2));
        validationPanel.add(resolvedRelativeToLabel, BorderLayout.NORTH);
        validationPanel.add(validationMessagesArea, BorderLayout.CENTER);

        final JBPanel<?> contentPanel = new JBPanel<>(new BorderLayout(0, 4));
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(validationPanel, BorderLayout.SOUTH);

        additionalPathsPanel.add(contentPanel, BorderLayout.CENTER);
        panel.add(additionalPathsPanel, constraints);
        return row + 1;
    }

    private static int addLabeledComponentRow(
            final JBPanel<?> panel,
            final GridBagConstraints constraints,
            final int row,
            final String label,
            final JComponent field,
            final int topInset
    ) {
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.gridwidth = 1;
        constraints.weightx = 0.0;
        constraints.insets = new Insets(topInset, 0, 0, 8);
        panel.add(new JBLabel(label), constraints);

        constraints.gridx = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1.0;
        constraints.insets = new Insets(topInset, 0, 0, 0);
        panel.add(field, constraints);
        return row + 1;
    }

    private static int addIndentedComponentRow(
            final JBPanel<?> panel,
            final GridBagConstraints constraints,
            final int row,
            final JComponent component,
            final int topInset
    ) {
        return addLabeledComponentRow(panel, constraints, row, "", component, topInset);
    }

    private static int addHelpRow(
            final JBPanel<?> panel,
            final GridBagConstraints constraints,
            final int row,
            final String text
    ) {
        constraints.gridy = row;
        constraints.gridx = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1.0;
        constraints.insets = new Insets(4, 0, 0, 0);
        final JBLabel helpLabel = new JBLabel(text);
        helpLabel.setFont(helpLabel.getFont().deriveFont(helpLabel.getFont().getSize2D() - 1.0f));
        helpLabel.setForeground(helpLabel.getForeground().darker());
        panel.add(helpLabel, constraints);
        return row + 1;
    }

    private static int addSectionSeparator(
            final JBPanel<?> panel,
            final GridBagConstraints constraints,
            final int row
    ) {
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1.0;
        constraints.insets = new Insets(12, 0, 8, 0);
        panel.add(createSeparator(), constraints);
        return row + 1;
    }

    private static int addSectionHeader(
            final JBPanel<?> panel,
            final GridBagConstraints constraints,
            final int row,
            final String text
    ) {
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1.0;
        constraints.insets = new Insets(0, 0, 2, 0);
        final JBLabel label = new JBLabel(text);
        final Font font = label.getFont();
        label.setFont(font.deriveFont(Font.BOLD, font.getSize2D()));
        panel.add(label, constraints);
        return row + 1;
    }

    private static JComponent createSeparator() {
        return new JSeparator(SwingConstants.HORIZONTAL);
    }
}
