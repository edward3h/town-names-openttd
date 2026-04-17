package red.ethel.townnames.bananas;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Extracts a single file by extension from an uncompressed TAR archive. */
final class TarExtractor {

    private TarExtractor() {}

    /**
     * Extracts the first {@code .grf} entry from the given TAR bytes.
     *
     * @throws IOException if no {@code .grf} entry is found or the archive is malformed
     */
    static byte[] extractGrf(byte[] tar) throws IOException {
        int offset = 0;
        while (offset + 512 <= tar.length) {
            if (isZeroBlock(tar, offset)) break;

            String name = readNullTerminatedString(tar, offset, 100);
            String sizeStr = readNullTerminatedString(tar, offset + 124, 12).trim();
            char type = (char) (tar[offset + 156] & 0xFF);
            int size = sizeStr.isEmpty() ? 0 : Integer.parseInt(sizeStr, 8);
            int dataOffset = offset + 512;

            if (type != '5' && name.endsWith(".grf")) {
                return Arrays.copyOfRange(tar, dataOffset, dataOffset + size);
            }

            int paddedSize = ((size + 511) / 512) * 512;
            offset = dataOffset + paddedSize;
        }
        throw new IOException("No .grf file found in tar archive");
    }

    private static String readNullTerminatedString(byte[] data, int offset, int maxLen) {
        int end = offset;
        while (end < offset + maxLen && data[end] != 0) end++;
        return new String(data, offset, end - offset, StandardCharsets.US_ASCII);
    }

    private static boolean isZeroBlock(byte[] data, int offset) {
        for (int i = offset; i < offset + 512; i++) {
            if (data[i] != 0) return false;
        }
        return true;
    }
}
