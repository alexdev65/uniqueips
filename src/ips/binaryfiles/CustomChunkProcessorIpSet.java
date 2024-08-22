package ips.binaryfiles;

import ips.IpParser;
import ips.IpSet;
import ips.Stat;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

/**
 * See CustomChunkProcessorBase.
 * Uses IpSet as temporary storage
 */
public class CustomChunkProcessorIpSet extends CustomChunkProcessorBase {
    private IpSet ipSet;
    private final Supplier<? extends IpSet> ipSetSupplier;

    public CustomChunkProcessorIpSet(ByteBufferProvider byteBufferProvider,
                                     IpSet globalSet, Stat stat, IpParser ipParser,
                                     Supplier<? extends IpSet> ipSetSupplier) {
        super(byteBufferProvider, globalSet, stat, ipParser);
        this.ipSetSupplier = ipSetSupplier;
        clearSet();
    }

    @Override
    protected void prepareForBufferProcessing(ByteBuffer buffer) {

    }

    @Override
    protected void syncToGlobalSet() {
        globalSet.merge(ipSet);
    }

    @Override
    protected void processLine() {
        ipParser.ipToOctetsFast(ipv4.octets, lineBytes, 0, lineLength);
        ipSet.add(ipv4);
        lines++;
    }

    @Override
    protected void clearSet() {
        ipSet = ipSetSupplier.get(); //< TODO: optimize memory allocations
    }

}
