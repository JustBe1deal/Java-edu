package coursehub.practice7;

import java.io.DataInputStream;
import java.io.IOException;

/** Общие константы и проверка количества для бинарных форматов. */
final class BinaryFormatSupport {
    static final int MAGIC = 0x434F5552; // ASCII COUR
    static final int MAX_RECORDS = 1_000_000;

    private BinaryFormatSupport() {}

    static int count(DataInputStream in) throws IOException {
        int n = in.readInt();
        if (n < 0 || n > MAX_RECORDS) throw new IOException("Некорректное число записей: " + n);
        return n;
    }
}
