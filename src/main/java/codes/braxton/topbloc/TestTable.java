package codes.braxton.topbloc;

import com.google.gson.Gson;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

import java.io.File;
import java.util.Arrays;

/**
 * The TestTable class contains all the information read from the
 * Excel-formatted file provided in the challenge description, sans
 * headers. It also contains the logic for combining them per the
 * specification.
 */
public class TestTable {

    /** numberSetOne in the provided spreadsheets. */
    private final double[] col1Nums;

    /** numberSetTwo in the provided spreadsheets. */
    private final double[] col2Nums;

    /** wordSetOne in the provided spreadsheets. */
    private final String[] col3Strs;

    private TestTable(double[] col1Nums,
                      double[] col2Nums,
                      String[] col3Strs) {
        this.col1Nums = col1Nums;
        this.col2Nums = col2Nums;
        this.col3Strs = col3Strs;
    }

    /**
     * Static wrapper around private constructor. Ensures that any
     * reference to a <code>TestTable</code> has exactly the same
     * length for each of its column fields.
     *
     * @param  col1Nums contents of numberSetOne column
     * @param  col2Nums contents of numberSetTwo column
     * @param  col3Strs contents of wordsSetOne column
     * @return          valid <code>TestTable</code> with columns guaranteed to
     *                  have the same length.
     */
    public static TestTable createWith(double[] col1Nums,
                                       double[] col2Nums,
                                       String[] col3Strs)
            throws TestTableException {
        if (col1Nums.length == col2Nums.length
            && col2Nums.length == col3Strs.length) {
            return new TestTable(col1Nums, col2Nums, col3Strs);
        }

        throw TestTableException.paramLengthMismatch(col1Nums, col2Nums, col3Strs);
    }

    /**
     * Reads table from workbook at provided path.
     *
     * @param filePath workbook location
     * @return         <code>TestTable</code> read from provided path
     */
    public static TestTable readFrom(String filePath)
            throws TestTableException {
        Workbook workbook = null;
        try {
            workbook = WorkbookFactory.create(new File(filePath));
        } catch (Exception e) {
            throw TestTableException.workbookReadExc(filePath, e);
        }

        if (workbook.getNumberOfSheets() < 1) {
            throw TestTableException.emptyWorkbookExc(workbook);
        }

        Sheet firstSheet = workbook.getSheetAt(0);

        TestTable table = readFrom(firstSheet);

        try { workbook.close(); }
        catch (java.io.IOException e) {} // this only throws for writeable packages

        return table;
    }

    /**
     * Reads table from provided sheet. Assumes first row contains
     * headers, which are ignored, and includes all data from the
     * first row in the sheet to the last.
     *
     * @param sheet spreadsheet from which to read table
     * @return      <code>TestTable</code> read from provided sheet
     */
    public static TestTable readFrom(Sheet sheet)
            throws TestTableException {
        int firstRowNum = sheet.getFirstRowNum() + 1; // ignore header row
        int lastRowNum = sheet.getLastRowNum();

        return readFrom(sheet, firstRowNum, lastRowNum);
    }

    /**
     * Reads table from provided sheet, starting and stopping and
     * provided row indices.
     *
     * @param sheet       spreadsheet from which to read table
     * @param firstRowNum index of first row containing table data
     * @param lastRowNum  index of last row containing table data
     * @return            <code>TestTable</code> read from provided sheet from
     *                    specified row range
     */
    public static TestTable readFrom(Sheet sheet, int firstRowNum, int lastRowNum)
            throws TestTableException {
        int numRows = Math.max(lastRowNum - firstRowNum + 1, 0);

        double[] col1Nums = new double[numRows];
        double[] col2Nums = new double[numRows];
        String[] col3Strs = new String[numRows];

        for (int i = 0; i < numRows; ++i) {
            int rowNum = firstRowNum + i;
            Row row = sheet.getRow(rowNum);

            if (row == null) {
                throw TestTableException.missingRowExc(sheet, rowNum);
            }

            col1Nums[i] = getNumericCellValue(row, 0);
            col2Nums[i] = getNumericCellValue(row, 1);
            col3Strs[i] = getStringCellValue(row, 2);
        }

        return new TestTable(col1Nums, col2Nums, col3Strs);
    }

    /**
     * Gets numeric value of cell in provided row at specified cell
     * index.
     *
     * @param row     row in which cell is contained
     * @param cellNum index of the cell for which to obtain the
     *                numeric value
     * @return        numeric cell value of cell at specified cell
     *                index in given row
     */
    private static double getNumericCellValue(Row row, int cellNum)
            throws TestTableException {
        Cell cell = row.getCell(cellNum);

        if (cell == null) {
            throw TestTableException.missingCellExc(row, cellNum);
        }

        double numVal;
        try {
            numVal = cell.getNumericCellValue();
        } catch (IllegalStateException e) {
            throw TestTableException.cellValueExc(cell, CellType.NUMERIC);
        } catch (NumberFormatException e) {
            throw TestTableException.cellValueExc(cell, CellType.NUMERIC);
        }

        return numVal;
    }

    /**
     * Gets string value of cell in provided row at specified cell
     * index.
     *
     * @param row     row in which cell is contained
     * @param cellNum index of the cell for which to obtain the
     *                string value
     * @return        string cell value of cell at specified cell
     *                index in given row
     */
    private static String getStringCellValue(Row row, int cellNum)
            throws TestTableException {
        Cell cell = row.getCell(cellNum);

        if (cell == null) {
            throw TestTableException.missingCellExc(row, cellNum);
        }

        String strVal;
        try {
            strVal = cell.getStringCellValue();
        } catch (IllegalStateException e) {
            throw TestTableException.cellValueExc(cell, CellType.STRING);
        } catch (NumberFormatException e) {
            throw TestTableException.cellValueExc(cell, CellType.STRING);
        }

        return strVal;
    }

    /**
     * Prints values contained within the table; primarily for
     * debugging.
     */
    public void printValues() {
        for (int i = 0; i < col1Nums.length; ++i) {
            System.out.println(String.valueOf(col1Nums[i])
                               + "\t" + String.valueOf(col2Nums[i])
                               + "\t" + col3Strs[i]);
        }
    }

    /**
     * Gets number of rows in the table.
     */
    public int getNumRows() {
        return col1Nums.length;
    }

    /**
     * Gets contents of the first column, numberSetOne in the provided
     * spreadsheets.
     */
    public double[] getCol1Nums() {
        return Arrays.copyOf(col1Nums, col1Nums.length);
    }

    /**
     * Gets contents of the second column, numberSetTwo in the
     * provided spreadsheets.
     */
    public double[] getCol2Nums() {
        return Arrays.copyOf(col2Nums, col2Nums.length);
    }

    /**
     * Gets contents of the third column, wordSetOne in the provided
     * spreadsheets.
     */
    public String[] getCol3Strs() {
        return Arrays.copyOf(col3Strs, col3Strs.length);
    }

    /**
     * Combines the tables per the instructions in the challenge
     * description.
     *
     * @param t1 first table
     * @param t2 second table
     * @return   new table formed by pairwise multiplication of the two
     *           tables' first column, pairwise division of their
     *           second columns, and pairwise concatenation of their
     *           third columns
     */
    public static TestTable combine(TestTable t1, TestTable t2)
            throws TestTableException {
        if (t1.getNumRows() != t2.getNumRows()) {
            throw TestTableException.tableLengthMismatch(t1, t2);
        }

        int numRows = t1.getNumRows();
        double[] col1Nums = new double[numRows];
        double[] col2Nums = new double[numRows];
        String[] col3Strs = new String[numRows];

        for (int i = 0; i < numRows; ++i) {
            col1Nums[i] = t1.col1Nums[i] * t2.col1Nums[i];
            col2Nums[i] = t1.col2Nums[i] / t2.col2Nums[i];
            col3Strs[i] = t1.col3Strs[i] + " " + t2.col3Strs[i];
        }

        return new TestTable(col1Nums, col2Nums, col3Strs);
    }

    /**
     * The <code>RequestContent</code> class encapsulates all the data
     * to be sent within the HTTP request to the application server,
     * and simplifies the matter of serialization by having field
     * names matching that specified in the challenge description.  By
     * using reflection, GSON is able to map these to JSON key names,
     * and the values are serialized as one would expect.
     *
     * This is a separate class because conceptually a
     * <code>TestTable</code> does not have any association with an
     * email address, and this would unnecessarily complicate the
     * logic involved in combination and read/writing, which would
     * require many choices to be made which were entirely arbitrary,
     * e.g. a default email address to be used when the
     * <code>TestTable</code> was read from one of the challenge
     * workbooks which contained no such data, or how to combine
     * emails in the implementation of <code>combine</code>.
     */
    private static class RequestContent {

        public final String id;
        public final double[] numberSetOne;
        public final double[] numberSetTwo;
        public final String[] wordSetOne;

        public RequestContent(String id,
                              double[] numberSetOne,
                              double[] numberSetTwo,
                              String[] wordSetOne) {
            this.id           = id;
            this.numberSetOne = numberSetOne;
            this.numberSetTwo = numberSetTwo;
            this.wordSetOne   = wordSetOne;
        }

    }

    /**
     * Computes JSON-encoded body of request to be sent to application
     * server, which will contain the provided email and the contents
     * of the table.
     *
     * @param email email to include in request body's <code>"id"</code> key
     * @return      <code>String</code> with JSON-encoded application
     *              request body
     */
    public String createRequestJson(String email) {
        RequestContent req = new RequestContent(email, col1Nums, col2Nums, col3Strs);
        return new Gson().toJson(req);
    }

}
