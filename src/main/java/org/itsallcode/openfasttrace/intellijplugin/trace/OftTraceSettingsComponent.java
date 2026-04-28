package org.itsallcode.openfasttrace.intellijplugin.trace;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.FormBuilder;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.nio.file.Path;

final class OftTraceSettingsComponent {
    private final JBRadioButton wholeProjectRadioButton =
            new JBRadioButton("Trace the whole project");
    private final JBRadioButton selectedResourcesRadioButton =
            new JBRadioButton("Trace selected resources");
    private final JBCheckBox includeSourceRootsCheckBox =
            new JBCheckBox("Include IntelliJ source directories");
    private final JBCheckBox includeTestRootsCheckBox =
            new JBCheckBox("Include IntelliJ test directories");
    private final JBTextArea additionalPathsTextArea = new JBTextArea();
    private final JBLabel resolvedRelativeToLabel = new JBLabel();
    private final JBTextArea validationMessagesArea = new JBTextArea();
    private final Path projectRoot;
    private final JPanel panel;

    OftTraceSettingsComponent(final Path projectRoot) {
        this.projectRoot = projectRoot;
        final ButtonGroup traceScopeGroup = new ButtonGroup();
        traceScopeGroup.add(wholeProjectRadioButton);
        traceScopeGroup.add(selectedResourcesRadioButton);
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
        panel = FormBuilder.createFormBuilder()
                .addComponent(wholeProjectRadioButton)
                .addComponent(selectedResourcesRadioButton)
                .addComponent(includeSourceRootsCheckBox, 1)
                .addComponent(includeTestRootsCheckBox, 1)
                .addComponent(additionalPathsPanel, 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        setSettings(new OftTraceSettingsSnapshot(
                OftTraceScopeMode.WHOLE_PROJECT,
                true,
                true,
                OftTraceProjectSettings.DEFAULT_ADDITIONAL_PATH
        ));
    }

    JComponent getPanel() {
        return panel;
    }

    OftTraceSettingsSnapshot getSettings() {
        return new OftTraceSettingsSnapshot(
                selectedResourcesRadioButton.isSelected()
                        ? OftTraceScopeMode.SELECTED_RESOURCES
                        : OftTraceScopeMode.WHOLE_PROJECT,
                includeSourceRootsCheckBox.isSelected(),
                includeTestRootsCheckBox.isSelected(),
                additionalPathsTextArea.getText()
        );
    }

    void setSettings(final OftTraceSettingsSnapshot settings) {
        wholeProjectRadioButton.setSelected(settings.scopeMode() == OftTraceScopeMode.WHOLE_PROJECT);
        selectedResourcesRadioButton.setSelected(settings.scopeMode() == OftTraceScopeMode.SELECTED_RESOURCES);
        includeSourceRootsCheckBox.setSelected(settings.includeSourceRoots());
        includeTestRootsCheckBox.setSelected(settings.includeTestRoots());
        additionalPathsTextArea.setText(settings.additionalPathsText());
        updateSelectedResourcesEnabledState();
    }

    boolean isSelectedResourcesEnabled() {
        return includeSourceRootsCheckBox.isEnabled()
                && includeTestRootsCheckBox.isEnabled()
                && additionalPathsTextArea.isEnabled();
    }

    String resolvedRelativeToText() {
        return resolvedRelativeToLabel.getText();
    }

    String validationMessagesText() {
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
