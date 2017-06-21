package pl.lodz.p.fit;

import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.CoderException;
import org.apache.beam.sdk.util.SerializableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class SerializableCoder<T extends Serializable> extends Coder<T> {

    private final Logger logger = LoggerFactory.getLogger(SerializableCoder.class);
    private final Class<T> tClass;

    public SerializableCoder(Class<T> tClass) {
        this.tClass = tClass;
    }

    @Override
    public void encode(T value, OutputStream outStream) throws CoderException, IOException {
        byte[] bytes = SerializableUtils.serializeToByteArray(value);
        outStream.write(bytes.length);
        outStream.write(bytes);
    }

    @Override
    public T decode(InputStream inStream) throws CoderException, IOException {
        int size = inStream.read();
        byte[] buffer = new byte[size];
        int read = inStream.read(buffer);

        if (size != read) {
            logger.warn("suspicious deserialize: class={}, size={}, read={}", tClass.getName(), size, read);
        }

        Object object = SerializableUtils.deserializeFromByteArray(buffer, tClass.getName());
        return tClass.cast(object);
    }

    @Override
    public List<? extends Coder<?>> getCoderArguments() {
        return Collections.emptyList();
    }

    @Override
    public void verifyDeterministic() throws NonDeterministicException {

    }
}
