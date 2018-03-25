package codes.braxton.topbloc;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

/**
 * This class represents any exceptions which may arise during the
 * reading, combination, creation, or other handling of
 * <code>TestTable</code>s. Static methods are provided for specific
 * instances of exceptions that may be encountered, which might later
 * be refactored into individual subclasses.
 */
public class TestTableException extends Exception {

    private TestTableException() { super(); }

    private TestTableException(String message) { super(message); }

    private TestTableException(String message, Throwable cause) {
        super(message, cause);
    }

    public static TestTableException paramLengthMismatch(double[] col1Nums,
                                                         double[] col2Nums,
                                                         String[] col3Strs) {
        return new TestTableException("Attempted to create TestTable"
                                      + "with parameters of differing lengths");
    }

    public static TestTableException tableLengthMismatch(TestTable t1, TestTable t2) {
        return new TestTableException("Attempted to combine TestTables of differing lengths");
    }

    public static TestTableException workbookReadExc(String filePath, Throwable cause) {
        return new TestTableException("Failed to read workbook " + filePath, cause);
    }

    public static TestTableException emptyWorkbookExc(Workbook workbook) {
        return new TestTableException("Workbook empty");
    }

    public static TestTableException missingRowExc(Sheet sheet, int rowNum) {
        return new TestTableException("Missing row " + String.valueOf(rowNum));
    }

    public static TestTableException missingCellExc(Row row, int cellNum) {
        return new TestTableException("Missing cell "
                                      + String.valueOf(cellNum)
                                      + " in row "
                                      + String.valueOf(row.getRowNum()));
    }

    public static TestTableException cellValueExc(Cell cell, CellType expectedType) {
        return new TestTableException("Invalid cell value type for cell in row "
                                      + String.valueOf(cell.getRowIndex())
                                      + ", column "
                                      + String.valueOf(cell.getColumnIndex())
                                      + ": expected type "
                                      + expectedType.toString());
    }

}
