package dev.skydynamic.quickbackupmulti.storage.codec;

import dev.skydynamic.quickbackupmulti.storage.DimensionFormat;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.HashMap;
import java.util.Map;

public class DimensionFormatCodec implements Codec<DimensionFormat> {
    @Override
    public Class<DimensionFormat> getEncoderClass() {
        return DimensionFormat.class;
    }

    /**
     * Encode an instance of the type parameter {@code T} into a BSON value.
     *
     * @param writer         the BSON writer to encode into
     * @param value          the value to encode
     * @param encoderContext the encoder context
     */
    @Override
    public void encode(BsonWriter writer, DimensionFormat value, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writeMap(writer, "poi", value.getPoi());
        writeMap(writer, "entities", value.getEntities());
        writeMap(writer, "region", value.getRegion());
        writeMap(writer, "data", value.getData());
        writer.writeEndDocument();
    }

    /**
     * Decodes a BSON value from the given reader into an instance of the type parameter {@code T}.
     *
     * @param reader         the BSON reader
     * @param decoderContext the decoder context
     * @return an instance of the type parameter {@code T}.
     */
    @Override
    public DimensionFormat decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartDocument();
        DimensionFormat dimensionFormat = new DimensionFormat();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            switch (fieldName) {
                case "poi":
                    dimensionFormat.setPoi(readMap(reader));
                    break;
                case "entities":
                    dimensionFormat.setEntities(readMap(reader));
                    break;
                case "region":
                    dimensionFormat.setRegion(readMap(reader));
                    break;
                case "data":
                    dimensionFormat.setData(readMap(reader));
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.readEndDocument();
        return dimensionFormat;
    }

    private void writeMap(BsonWriter writer, String name, Map<String, String> map) {
        writer.writeName(name);
        writer.writeStartDocument();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            writer.writeName(entry.getKey());
            writer.writeString(entry.getValue());
        }
        writer.writeEndDocument();
    }

    private HashMap<String, String> readMap(BsonReader reader) {
        HashMap<String, String> map = new HashMap<>();
        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            map.put(reader.readName(), reader.readString());
        }
        reader.readEndDocument();
        return map;
    }
}
