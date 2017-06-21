package pl.lodz.p.mgr;


import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.CoderException;
import org.apache.beam.sdk.util.SerializableUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NiceCoder<T extends Serializable> extends Coder<T> {

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

        return (T) SerializableUtils.deserializeFromByteArray(buffer, "X");
    }

    @Override
    public List<? extends Coder<?>> getCoderArguments() {
        return new ArrayList<>();
    }

    @Override
    public void verifyDeterministic() throws NonDeterministicException {

    }
}
