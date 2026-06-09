package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
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
    private final JBRadioButton plainTextResultViewRadioButton =
            new JBRadioButton("Plain text output");
    private final JBRadioButton testRunnerResultViewRadioButton =
            new JBRadioButton("IntelliJ Test Runner UI");
    private final JBLabel resolvedRelativeToLabel = new JBLabel();
    private final JBTextArea validationMessagesArea = new JBTextArea();
    private final Path projectRoot;
    private final boolean showResultViewSelection;
    private final JPanel panel;

    public OftTraceSettingsComponent(final Path projectRoot) {
        this(projectRoot, false);
    }

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
        final JBPanel<?> additionalPathsPanel = new JBPanel<>(new BorderLayout(0, 4));
        additionalPathsPanel.add(
                new JBLabel("Additional project-relative files or directories (one per line)"),
                BorderLayout.NORTH
        );
        additionalPathsPanel.add(scrollPane, BorderLayout.CENTER);
        final JBPanel<?> additionalPathsFeedbackPanel = new JBPanel<>(new BorderLayout(0, 4));
        additionalPathsFeedbackPanel.add(resolvedRelativeToLabel, BorderLayout.NORTH);
        additionalPathsFeedbackPanel.add(validationMessagesArea, BorderLayout.CENTER);
        additionalPathsPanel.add(additionalPathsFeedbackPanel, BorderLayout.SOUTH);
        final FormBuilder formBuilder = FormBuilder.createFormBuilder()
                .addComponent(wholeProjectRadioButton)
                .addComponent(selectedResourcesRadioButton)
                .addComponent(includeSourceRootsCheckBox, 1)
                .addComponent(includeTestRootsCheckBox, 1)
                .addComponent(additionalPathsPanel, 1)
                .addSeparator()
                .addLabeledComponent("Artifact types:", artifactTypesField)
                .addTooltip("comma-separated, empty = all")
                .addLabeledComponent("Tags:", tagsField)
                .addTooltip("comma-separated, empty = all");
        if (showResultViewSelection) {
            formBuilder
                    .addSeparator()
                    .addComponent(new JBLabel("Result view"))
                    .addComponent(plainTextResultViewRadioButton, 1)
                    .addComponent(testRunnerResultViewRadioButton, 1);
        }
        panel = formBuilder
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
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
        plainTextResultViewRadioButton.setSelected(settings.resultView() == OftTraceResultView.PLAIN_TEXT);
        testRunnerResultViewRadioButton.setSelected(settings.resultView() == OftTraceResultView.TEST_RUNNER);
        updateSelectedResourcesEnabledState();
    }

    public boolean isSelectedResourcesEnabled() {
        return includeSourceRootsCheckBox.isEnabled()
                && includeTestRootsCheckBox.isEnabled()
                && additionalPathsTextArea.isEnabled();
    }

    public String resolvedRelativeToText() {
        return resolvedRelativeToLabel.getText();
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
            return;
        }
        final OftAdditionalTracePathValidation validation =
                OftAdditionalTracePathValidation.validate(projectRoot, additionalPathsTextArea.getText());
        resolvedRelativeToLabel.setText(validation.resolvedRelativeToText());
        validationMessagesArea.setText(String.join(System.lineSeparator(), validation.messages()));
    }
}
