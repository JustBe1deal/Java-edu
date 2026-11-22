package coursehub.practice6;

/** Два способа собрать одинаковый текстовый отчёт. */
public final class ReportBuilder {
    private ReportBuilder() {}
    /** Собирает строки через изменяемый StringBuilder. */
    public static String reportBuilder(int lines) {
        if (lines < 0) throw new IllegalArgumentException("Отрицательное количество");
        StringBuilder b = new StringBuilder(); for (int i = 0; i < lines; i++) b.append("Строка ").append(i).append('\n'); return b.toString();
    }
    /** Собирает строки конкатенацией для учебного сравнения. */
    public static String reportPlus(int lines) {
        if (lines < 0) throw new IllegalArgumentException("Отрицательное количество");
        String report = ""; for (int i = 0; i < lines; i++) report += "Строка " + i + '\n'; return report;
    }
}
