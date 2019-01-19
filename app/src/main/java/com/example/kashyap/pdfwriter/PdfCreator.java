package com.example.kashyap.pdfwriter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pdfwriter.FontSize;
import pdfwriter.PDFWriter;
import pdfwriter.PaperSize;
import pdfwriter.PixelCalculator;
import pdfwriter.StandardFonts;


class PdfCreator extends Writer {
    private final int PADDING = 60;
    private final int PAGE_STARTING_PIXEL = PADDING / 2;
    private final int PAGE_ENDING_PIXEL = PaperSize.A4_HEIGHT - (PADDING / 2);
    private final int ROW_START_PIXEL = PADDING / 2;
    private final int ROW_END_PIXEL = PaperSize.A4_WIDTH - (PADDING / 2);
    private Context mContext;
    private PDFWriter mPDFWriter;
    private int mCursorY;
    private int width;
    private int height;
    //private final int CART_ITEM_HEADER_PIXEL_POSITION[] = new int[]{35, 270, 340, 410, 480, 550};
    private final int CART_ITEM_HEADER_PIXEL_POSITION[] = new int[]{ROW_START_PIXEL, 250, 320, 390, 460, 510};
    private final int MAX_HEIGHT_OF_A_CART_ITEM_ROW = 50;
    private final int MAX_HEIGHT_OF_A_DISCOUNT_TOTAL_PRICE_DETAILS = 50;
    private final int MAX_HEIGHT_OF_A_TAX_DETAILS = 150;
    private final int MAX_HEIGHT_OF_A_PAYMENT_DETAILS = 150;
    private final int MAX_HEIGHT_OF_A_CUSTOM_NOTES = 70;

    PdfCreator(Context context) {
        super(context);
        this.mContext = context;
        resetPageHeight();
        String pdfcontent = generatePDF();
        outputToFile(pdfcontent, "ISO-8859-1");
    }


    private String generatePDF() {

        mPDFWriter = new PDFWriter(PaperSize.A4_WIDTH, PaperSize.A4_HEIGHT);


        preparePDFString();

        printPageNo();
        return mPDFWriter.asString();
    }

    private void printPageNo() {
        int pageCount = mPDFWriter.getPageCount();
        for (int i = 0; i < pageCount; i++) {
            mPDFWriter.setCurrentPage(i);
            mPDFWriter.addText(25, 25, 10, Integer.toString(i + 1) + " / " + Integer.toString(pageCount) + " Invoice No.: " + "AX1/2015/110");
        }
    }

    private void printFirstPageHeader() {
        headerPart();
        appendHorizontalLine();
        printCustomerDetails();
        appendHorizontalLine();
        addCartItemTableHeader();
        appendHorizontalLine();
    }

    private void preparePDFString() {
        ArrayList<String[]> dataSet = prepareCartItems();
        printFirstPageHeader();
        for (String row[] : dataSet) {
            height = PixelCalculator.getPixelHeight(row[1], FontSize.FONT_10);
            mCursorY = mCursorY - height - FontSize.FONT_10 / 2; // for line spacing.
            if (haveGAPToFillCartItem()) {
                for (int i = 1; i < CART_ITEM_HEADER_PIXEL_POSITION.length; i++) {
                    mPDFWriter.addText(CART_ITEM_HEADER_PIXEL_POSITION[i], mCursorY, FontSize.FONT_10, row[i]);
                }
                int splitAtIndex = 30;
                if (row[0].length() >= splitAtIndex) {
                    List<String> strings = new ArrayList<String>();
                    int index = 0;
                    int currentHeight = mCursorY;
                    while (index < row[0].length()) {
                        strings.add(row[0].substring(index, Math.min(index + splitAtIndex, row[0].length())));
                        index += splitAtIndex;
                    }
                    for (String line : strings) {
                        mPDFWriter.addText(CART_ITEM_HEADER_PIXEL_POSITION[0], currentHeight, FontSize.FONT_10, line);
                        currentHeight = currentHeight - height - FontSize.FONT_10 / 2; // for line spacing.
                    }
                    mCursorY = currentHeight;
                } else {
                    mPDFWriter.addText(CART_ITEM_HEADER_PIXEL_POSITION[0], mCursorY, FontSize.FONT_10, row[0]);
                }
            } else {
                mPDFWriter.newPage();
                //reset the current page gap from bottom.
                resetPageHeight();
                headerPart();
                appendHorizontalLine();
                addCartItemTableHeader();
                appendHorizontalLine();
            }
        }
        //Checking we have gap to fill the cart details.
        if (checkWeHaveGapToFillDiscountTotalPriceDetails()) {
            appendHorizontalLine();
            printDiscountItemTotalPrice();
        } else {
            //NO SPACE GO FOR NEXT PAGE.
            mPDFWriter.newPage();
            //reset the current page gap from bottom.
            resetPageHeight();
            headerPart();
            appendHorizontalLine();
            printDiscountItemTotalPrice();
        }
        //Checking we have gap to fill the tax details.
        if (checkWeHaveGapToFillTaxDetails()) {
            printTaxDetails();
        } else {
            //NO SPACE GO FOR NEXT PAGE.
            mPDFWriter.newPage();
            //reset the current page gap from bottom.
            resetPageHeight();
            headerPart();

            //appendHorizontalLine();
            printTaxDetails();
        }

        if (checkWehaveGapToPaymentMethods()) {
            printPaymentMethods();
        } else {
            //NO SPACE GO FOR NEXT PAGE.
            mPDFWriter.newPage();
            //reset the current page gap from bottom.
            resetPageHeight();
            headerPart();
            printPaymentMethods();
        }
        if (checkWeHaveGapToFillCustomNotes())
            footerPart();
        else {
            //NO SPACE GO FOR NEXT PAGE.
            mPDFWriter.newPage();
            //reset the current page gap from bottom.
            resetPageHeight();
            headerPart();

        }
    }

    private boolean checkWeHaveGapToFillCustomNotes() {
        return mCursorY > MAX_HEIGHT_OF_A_CUSTOM_NOTES;
    }

    private void printPaymentMethods() {

        mCursorY = mCursorY - height - FontSize.FONT_10; // for line spacing.
        mPDFWriter.addText(CART_ITEM_HEADER_PIXEL_POSITION[0], mCursorY, FontSize.FONT_12, "Payment Method(s):");
        appendHorizontalLine();
        mCursorY = mCursorY - height - FontSize.FONT_10; // for line spacing.
        String paymentModes[] = {"Mode", "Amount"};
        int[] paymentRow = {ROW_START_PIXEL, 235};
        printPaymentRow(paymentRow, paymentModes);
        appendHorizontalLine();
        ArrayList<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"NPCI", "1390.00"});
        for (String row[] : rows) {
            mCursorY = mCursorY - height - FontSize.FONT_10; // for line spacing.
            printPaymentRow(paymentRow, row);

        }
        appendHorizontalLine();
    }

    private void printPaymentRow(int[] pixelPosition, String rows[]) {
        for (int i = 0; i < pixelPosition.length; i++) {
            mPDFWriter.addText(pixelPosition[i], mCursorY, FontSize.FONT_10, rows[i]);
        }
    }

    private boolean checkWehaveGapToPaymentMethods() {
        return mCursorY > MAX_HEIGHT_OF_A_PAYMENT_DETAILS;
    }

    private void printTaxDetails() {
        String taxInfo = "Tax Info";
        height = PixelCalculator.getPixelHeight(taxInfo);
        mCursorY = mCursorY - height - FontSize.FONT_10; // for line spacing.
        mPDFWriter.addText(CART_ITEM_HEADER_PIXEL_POSITION[0], mCursorY, FontSize.FONT_12, taxInfo);

        appendHorizontalLine();
        String taxHeaderPixels[] = new String[]{"Tax %", " Taxable", " CGST", " UGST/UTGST ", " Tax Amount"};
        int taxPixel[] = new int[]{ROW_START_PIXEL, 150, 235, 350, 490};
        mCursorY = mCursorY - height - FontSize.FONT_10; // for line spacing.

        for (int i = 0; i < taxPixel.length; i++) {
            mPDFWriter.addText(taxPixel[i], mCursorY, FontSize.FONT_12, taxHeaderPixels[i]);
        }
        appendHorizontalLine();
        mCursorY = mCursorY - height - FontSize.FONT_10; // for line spacing.
        taxPixel = new int[]{ROW_START_PIXEL, 155, 235, 355, 495};
        ArrayList<String[]> taxDetails = new ArrayList<>();
        taxDetails.add(new String[]{"0.00", "640.00", "0.00", "0.00", "0.00"});
        taxDetails.add(new String[]{"12.00", "42.86", "2.57", "2.57", "5.14"});
        taxDetails.add(new String[]{"28.00", "507.81", "71.09", "71.09", "142.19"});
        for (String[] row : taxDetails) {
            printTaxRow(taxPixel, row);
        }

        appendHorizontalLine();
        //height = PixelCalculator.getPixelHeight(taxDetails.get(0)[0]);
        mCursorY = mCursorY - height - FontSize.FONT_10; // for line spacing.
        String[] totalTax = new String[]{"Total Tax", "1190.67", "73.67", "73.67", "147.33"};
        printTaxRow(taxPixel, totalTax);
        appendHorizontalLine();
    }

    private void printTaxRow(int[] PixelPositions, String[] row) {

        for (int i = 0; i < PixelPositions.length; i++) {
            mPDFWriter.addText(PixelPositions[i], mCursorY, FontSize.FONT_10, row[i]);
        }
        height = PixelCalculator.getPixelHeight(row[0]);
        mCursorY = mCursorY - height - FontSize.FONT_5; // for line spacing.
    }

    private boolean checkWeHaveGapToFillTaxDetails() {
        return mCursorY > MAX_HEIGHT_OF_A_TAX_DETAILS;
    }

    private void printDiscountItemTotalPrice() {
        String itemQty = "Item(s) / Qty:7 / 13";
        //   height = PixelCalculator.getPixelHeight(itemQty);
        mCursorY = mCursorY - height - FontSize.FONT_10; // for line spacing.
        mPDFWriter.addText(CART_ITEM_HEADER_PIXEL_POSITION[0], mCursorY, FontSize.FONT_10, itemQty);

        ArrayMap<String, String> map = new ArrayMap<>();
        map.put("Additional Disc:", "0.00");
        map.put("Total:", "1390.00");
        map.put("Return Amount:", "0.00");


        int qtyDetailsPixels[] = new int[]{320, 510};
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println(entry.getKey() + "/" + entry.getValue());
            mPDFWriter.addText(qtyDetailsPixels[0], mCursorY, FontSize.FONT_10, entry.getKey());
            width = PixelCalculator.getPixelWidth(entry.getValue(), FontSize.FONT_10);
            mPDFWriter.addText(qtyDetailsPixels[1], mCursorY, FontSize.FONT_10, entry.getValue());
            mCursorY = mCursorY - height - FontSize.FONT_10 / 2; // for line spacing.
        }
        appendHorizontalLine();
        //mCursorY = mCursorY - height - FontSize.FONT_10 / 2;
    }

    private boolean checkWeHaveGapToFillDiscountTotalPriceDetails() {
        return mCursorY > MAX_HEIGHT_OF_A_DISCOUNT_TOTAL_PRICE_DETAILS;
    }

    private void headerPart() {
        addShopLogo();
        mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.TIMES_ROMAN);
        addShopTitle();
        addShopAddress();
        addShopGSTIN();
        addBillType();


    }

    private void footerPart() {
        //mCursorY = mCursorY - height - FontSize.FONT_10;
        //mPDFWriter.addLine(ROW_START_PIXEL, mCursorY, ROW_END_PIXEL, mCursorY);

        String notes = "NOTES";
        width = PixelCalculator.getPixelWidth(notes, FontSize.FONT_12);
        height = PixelCalculator.getPixelHeight(notes, FontSize.FONT_12);
        mCursorY = mCursorY - height - FontSize.FONT_10;//line spacing
        mPDFWriter.addText(getCenter(width), mCursorY, FontSize.FONT_12, notes);

        notes = "You have saved 85.32/-";
        width = PixelCalculator.getPixelWidth(notes, FontSize.FONT_15);
        height = PixelCalculator.getPixelHeight(notes, FontSize.FONT_15);
        mCursorY = mCursorY - height - FontSize.FONT_10;//line spacing
        mPDFWriter.addText(getCenter(width), mCursorY, FontSize.FONT_15, notes);

    }

    private ArrayList<String[]> prepareCartItems() {
        ArrayList<String[]> clients = new ArrayList<String[]>();
        for (int i = 0; i < 5; i++) {
            clients.add(new String[]{"Bru Coffee Super Strong/21010000", "330.00", "330.00", "28.00", "1.000", "33.00"});
            clients.add(new String[]{"Chocolate Chips Cookis/FWVYY9NT", "25.00", "25.00", "12.00", "1.000", "25.00"});
            clients.add(new String[]{"Thums Up/439VXFTX", "25.00", "23.00", "12.00", "1.000", "23.00"});
            clients.add(new String[]{"iphone", "120.00", "120.00", "0.00", "5.000", "600.00"});
            clients.add(new String[]{"Activ Apple- 200ml/2344", "26.00", "26.00", "22.00", "2.000", "52.00"});
            clients.add(new String[]{"A/g Pencial Box/39261000", "181.00", "160.00", "28.00", "2.000", "320.00"});
            clients.add(new String[]{"Bru Coffee Chicory Mixture 50g", "40.00", "40.00", "0.00", "1.000", "40.00"});
        }
        return clients;
    }

    private void addCartItemTableHeader() {
        String row[] = new String[]{"Item/HSN Code/Barcode", "MRP(INR)", "SP(INR)", "Tax %", "Qty", "Total(INR)"};
        mCursorY = mCursorY - height - FontSize.FONT_10;
        for (int i = 0; i < CART_ITEM_HEADER_PIXEL_POSITION.length; i++) {
            mPDFWriter.addText(CART_ITEM_HEADER_PIXEL_POSITION[i], mCursorY, FontSize.FONT_13, row[i]);
        }

    }

    private boolean haveGAPToFillCartItem() {
        Log.d("Page_Starting_Pixel", String.valueOf(PAGE_STARTING_PIXEL));
        return mCursorY > PAGE_STARTING_PIXEL && mCursorY > MAX_HEIGHT_OF_A_CART_ITEM_ROW;
    }


    private void resetPageHeight() {
        mCursorY = PaperSize.A4_HEIGHT - PAGE_STARTING_PIXEL;
    }

    private void printCustomerDetails() {

        ArrayList<String[]> customerDetails = new ArrayList<>();
        customerDetails.add(new String[]{"Customer Name: " + "Vivek S", "Invoice No.: " + "AX1/2015/110"});
        customerDetails.add(new String[]{"Mobile No: " + "8500156892", "Date: " + "27-12-2018"});
        customerDetails.add(new String[]{"GSTIN: " + "GSTIN12457890", "Time: " + "05:30 PM"});
        customerDetails.add(new String[]{"Address: " + "Hyderabad", "BillType : " + "RETAIL"});

        width = PixelCalculator.getPixelWidth(customerDetails.get(0)[0], FontSize.FONT_12);
        height = PixelCalculator.getPixelHeight(customerDetails.get(0)[0], FontSize.FONT_12);

        for (String[] row : customerDetails) {
            mCursorY = mCursorY - height - FontSize.FONT_10;
            mPDFWriter.addText(30, mCursorY, FontSize.FONT_12, row[0]);
            mPDFWriter.addText(ROW_END_PIXEL - width, mCursorY, FontSize.FONT_12, row[1]);
        }

    }

    private Toast toast;

    private void addShopLogo() {
        Bitmap image = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon);
        Log.i("pdf_logo", image.getWidth() + " " + image.getHeight());
        if (image.getHeight() > 100) {
            try {
                if (toast != null)
                    // To prevent long duration of toast queue we are maintaining single instance.
                    toast.cancel();
                toast = Toast.makeText(mContext, "Cannot add logo on pdf", Toast.LENGTH_SHORT);
                toast.show();
                //Currently it is running in UI Thread, so no issues.
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mCursorY = mCursorY - image.getHeight();
            mPDFWriter.addImage(getCenter(image.getWidth()), mCursorY, image);
        }
    }

    private void addShopTitle() {
        String shopName = "BHARAT SPORTS STARS";
        int width = PixelCalculator.getPixelWidth(shopName, FontSize.FONT_20);
        int height = PixelCalculator.getPixelHeight(shopName, FontSize.FONT_20);
        mCursorY = mCursorY - height-FontSize.FONT_10;
        mPDFWriter.addText(getCenter(width), mCursorY, FontSize.FONT_20, shopName);
    }


    private void addBillType() {
        String billType = "TAX INVOICE CUM BILL OF SUPPLY";
        width = PixelCalculator.getPixelWidth(billType, FontSize.FONT_15);
        height = PixelCalculator.getPixelHeight(billType, FontSize.FONT_15);
        mCursorY = mCursorY - height - FontSize.FONT_10;
        mPDFWriter.addText(getCenter(width), mCursorY, FontSize.FONT_15, billType);

    }

    private void addShopGSTIN() {
        String gstIn = "GSTIN: 36AAFCN4513K1ZE";
        mCursorY = mCursorY - PixelCalculator.getPixelHeight(gstIn, FontSize.FONT_12) - FontSize.FONT_10;
        mPDFWriter.addText(getCenter(PixelCalculator.getPixelWidth(gstIn, FontSize.FONT_12)), mCursorY, FontSize.FONT_12, gstIn);

    }

    private void addShopAddress() {
        int shopAddressSplitChar = 80;
        String shopAddress = "11th Floor, Watermark Technopark, Plot No.11, Survey No. 9, Whitefields, Kondapur, Hyderabad-500 084, Teleangana, India. Phone: +91 7997085678, E-mail: hello@nukkadhshops.com, Website: www.nukkadhsops.com";
        if (shopAddress.length() >= shopAddressSplitChar) {
            //Content is too much to display on UI so split the string into mulitple parts.
            List<String> strings = splitStringIntoMultipleParts(shopAddressSplitChar, shopAddress);
            for (String row : strings) {
                printShopAddress(row);
            }
        } else {
            printShopAddress(shopAddress);
        }

    }

    private void printShopAddress(String shopAddress) {
        width = PixelCalculator.getPixelWidth(shopAddress, FontSize.FONT_10);
        height = PixelCalculator.getPixelHeight(shopAddress, FontSize.FONT_10);
        int startX = getCenter(width);
        Log.d("Center_width_height", +startX + " " + width + " " + height);
        mCursorY = mCursorY - height - FontSize.FONT_10 / 2;
        mPDFWriter.addText(startX, mCursorY, FontSize.FONT_10, shopAddress);
    }


    private void appendHorizontalLine() {
        //add horizontal line

        mCursorY = mCursorY - FontSize.FONT_5;
        mPDFWriter.addLine(ROW_START_PIXEL, mCursorY, ROW_END_PIXEL, mCursorY);

    }

    private int subtractWidthFromTotalWidth(int width) {
        int usageSpace = ROW_END_PIXEL - ROW_START_PIXEL;
        return usageSpace - width;
    }

    private int getCenter(int width) {
        return ROW_START_PIXEL + subtractWidthFromTotalWidth(width) / 2;
    }


    private List<String> splitStringIntoMultipleParts(int splitIndex, String splitingText) {
        List<String> strings = new ArrayList<String>();
        int index = 0;
        while (index < splitingText.length()) {
            strings.add(splitingText.substring(index, Math.min(index + splitIndex, splitingText.length())));
            index += splitIndex;
        }

        return strings;
    }


}
