package fpprocessor.file;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import fpprocessor.ProgramLogger;

public class POIWriter {

	public static void writeWordReport(File file, Map<String, File> imageMap) {
		try (FileOutputStream out = new FileOutputStream(file)) {
			XWPFDocument document = new XWPFDocument();

			for (String title : imageMap.keySet()) {
				XWPFParagraph paragraph = document.createParagraph();
				XWPFRun run = paragraph.createRun();
				run.setFontFamily("Times New Roman");
				run.setFontSize(12);
				run.setText(title);
				run.addBreak();
				run.addBreak();

				File imgFile = imageMap.get(title);
				BufferedImage img = ImageIO.read(imgFile);
				
				// scales the image to 6.5 inches at 72 DPI
				double scaling = (img.getWidth() > 72 * 6.5) ? 72 * 6.5 / img.getWidth() : 1.0;
				try (FileInputStream in = new FileInputStream(imgFile)) {
					run.addPicture(in, XWPFDocument.PICTURE_TYPE_PNG, imgFile.getName(),
							Units.toEMU(img.getWidth() * scaling), Units.toEMU(img.getHeight() * scaling));
				}
				run.addBreak();
				run.addBreak(BreakType.PAGE);
			}
			document.write(out);
			document.close();
			ProgramLogger.log(POIWriter.class, ProgramLogger.INFO, "Exported Microsoft Word report: " + file.getAbsolutePath());
		} catch (IOException | InvalidFormatException e) {
			ProgramLogger.log(POIWriter.class, ProgramLogger.ERROR, "Unable to export Microsoft Word report: " + file.getAbsolutePath());
		}
	}
}
