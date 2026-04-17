package red.ethel.townnames.bananas;

/**
 * Uniquely identifies a NewGRF file on the Bananas content server.
 *
 * @param grfid the 8-character GRF identifier hex string
 * @param md5sum the MD5 checksum hex string of the file content
 */
public record ContentId(String grfid, String md5sum) {}
