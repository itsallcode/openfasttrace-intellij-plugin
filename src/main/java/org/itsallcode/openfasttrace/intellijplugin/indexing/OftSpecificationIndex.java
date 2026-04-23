package org.itsallcode.openfasttrace.intellijplugin.indexing;

import com.intellij.openapi.project.DumbAware;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileBasedIndexExtension;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.io.DataInputOutputUtil;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.itsallcode.openfasttrace.intellijplugin.OftSupportedFiles;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSpecificationItemMatch;
import org.itsallcode.openfasttrace.intellijplugin.syntax.OftSyntaxCore;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// [impl->dsn~specification-item-index~1]
public final class OftSpecificationIndex extends FileBasedIndexExtension<String, List<OftIndexedSpecification>> implements DumbAware {
    public static final ID<String, List<OftIndexedSpecification>> NAME = ID.create("openfasttrace.specification.index");

    private final DataIndexer<String, List<OftIndexedSpecification>, FileContent> indexer = inputData -> {
        if (!OftSupportedFiles.isSpecificationFileName(inputData.getFileName())) {
            return Map.of();
        }
        final Map<String, List<OftIndexedSpecification>> entries = new LinkedHashMap<>();
        for (OftSpecificationItemMatch match : OftSyntaxCore.findDefinitionSpecificationItems(inputData.getContentAsText())) {
            entries.computeIfAbsent(match.item().id(), ignored -> new ArrayList<>())
                    .add(new OftIndexedSpecification(
                            match.item().artifactType(),
                            match.item().name(),
                            match.item().revision(),
                            match.span().startOffset()
                    ));
        }
        entries.replaceAll((ignored, values) -> List.copyOf(values));
        return entries;
    };

    @Override
    public ID<String, List<OftIndexedSpecification>> getName() {
        return NAME;
    }

    @Override
    public DataIndexer<String, List<OftIndexedSpecification>, FileContent> getIndexer() {
        return indexer;
    }

    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public DataExternalizer<List<OftIndexedSpecification>> getValueExternalizer() {
        return new OftIndexedSpecificationExternalizer();
    }

    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return OftSupportedFiles::isSpecificationFile;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 3;
    }

    private static final class OftIndexedSpecificationExternalizer implements DataExternalizer<List<OftIndexedSpecification>> {
        @Override
        public void save(DataOutput out, List<OftIndexedSpecification> value) throws IOException {
            DataInputOutputUtil.writeINT(out, value.size());
            for (OftIndexedSpecification specification : value) {
                out.writeUTF(specification.artifactType());
                out.writeUTF(specification.name());
                DataInputOutputUtil.writeINT(out, specification.revision());
                DataInputOutputUtil.writeINT(out, specification.offset());
            }
        }

        @Override
        public List<OftIndexedSpecification> read(DataInput in) throws IOException {
            final int size = DataInputOutputUtil.readINT(in);
            final List<OftIndexedSpecification> values = new ArrayList<>(size);
            for (int index = 0; index < size; index++) {
                values.add(new OftIndexedSpecification(
                        in.readUTF(),
                        in.readUTF(),
                        DataInputOutputUtil.readINT(in),
                        DataInputOutputUtil.readINT(in)
                ));
            }
            return List.copyOf(values);
        }
    }
}
