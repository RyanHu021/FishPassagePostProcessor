package fpprocessor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

public class Config {

	private static File configFile;
	private static Properties props;
	private static HashMap<String, String> defaultProps;

	public static void init() throws IOException {
		configFile = new File("config.properties");
		if (!configFile.exists()) {
			configFile.createNewFile();
		}

		try (FileReader reader = new FileReader(configFile)) {
			defaultProps = new HashMap<String, String>();
			props = new Properties();
			props.load(reader);
		}

		defaultProps.put("hdfECNodePath", "/2DMeshModule/EC Mesh/Nodes/NodeLocs");
		defaultProps.put("hdfECElementPath", "/2DMeshModule/EC Mesh/Elements/Nodeids");
		defaultProps.put("hdfNCNodePath", "/2DMeshModule/NC Mesh/Nodes/NodeLocs");
		defaultProps.put("hdfNCElementPath", "/2DMeshModule/NC Mesh/Elements/Nodeids");
		defaultProps.put("hdfPCNodePath", "/2DMeshModule/PC Mesh/Nodes/NodeLocs");
		defaultProps.put("hdfPCElementPath", "/2DMeshModule/PC Mesh/Elements/Nodeids");
		defaultProps.put("hdfDepthPath", "/Water_Depth_ft/Values");
		defaultProps.put("hdfWSELPath", "/Water_Elev_ft/Values");
		defaultProps.put("hdfShearStressPath", "/B_Stress_lb_p_ft2/Values");
		defaultProps.put("hdfVelocityPath", "/Velocity/Values");
		defaultProps.put("pythonPath", "");
		defaultProps.put("localScriptPath", "scripts/");
		defaultProps.put("localRenderPath", "rendered/");
		defaultProps.put("useElementBasedTriangulation", "true");
		defaultProps.put("contourFillLevels", "50");
		defaultProps.put("contourLineLevels", "10");
		defaultProps.put("contourColorMap", "viridis");
		defaultProps.put("contourLineWidth", "0.1");
		defaultProps.put("colorBarScale", "0.5");
		defaultProps.put("plotPaddingPercentage", "0.05");
		defaultProps.put("imageDPI", "300");
		defaultProps.put("renderSVG", "false");
		refreshConfig();
	}

	public static void refreshConfig() throws IOException {
		try (FileWriter writer = new FileWriter(configFile)) {
			for (String key : defaultProps.keySet()) {
				if (!props.containsKey(key)) {
					props.setProperty(key, defaultProps.get(key));
				}
			}
			writer.flush();
			props.store(writer, null);
		}
	}

	public static String getHdfECNodePath() {
		return props.getProperty("hdfECNodePath");
	}

	public static String getHdfECElementPath() {
		return props.getProperty("hdfECElementPath");
	}

	public static String getHdfNCNodePath() {
		return props.getProperty("hdfNCNodePath");
	}

	public static String getHdfNCElementPath() {
		return props.getProperty("hdfNCElementPath");
	}

	public static String getHdfPCNodePath() {
		return props.getProperty("hdfPCNodePath");
	}

	public static String getHdfPCElementPath() {
		return props.getProperty("hdfPCElementPath");
	}

	public static String getHdfDepthPath() {
		return props.getProperty("hdfDepthPath");
	}

	public static String getHdfWSELPath() {
		return props.getProperty("hdfWSELPath");
	}

	public static String getHdfShearStressPath() {
		return props.getProperty("hdfShearStressPath");
	}

	public static String getHdfVelocityPath() {
		return props.getProperty("hdfVelocityPath");
	}

	public static String getPythonPath() {
		return props.getProperty("pythonPath");
	}

	public static String getLocalScriptPath() {
		return props.getProperty("localScriptPath");
	}
	
	public static String getLocalRenderPath() {
		return props.getProperty("localRenderPath");
	}

	public static boolean useElementBasedTriangulation() {
		return Boolean.parseBoolean(props.getProperty("useElementBasedTriangulation"));
	}

	public static int getContourFillLevels() {
		return Integer.parseInt(props.getProperty("contourFillLevels"));
	}

	public static int getContourLineLevels() {
		return Integer.parseInt(props.getProperty("contourLineLevels"));
	}

	public static String getContourColorMap() {
		return props.getProperty("contourColorMap");
	}

	public static double getContourLineWidth() {
		return Double.parseDouble(props.getProperty("contourLineWidth"));
	}
	
	public static double getColorBarScale() {
		return Double.parseDouble(props.getProperty("colorBarScale"));
	}
	
	public static double getPlotPaddingPercentage() {
		return Double.parseDouble(props.getProperty("plotPaddingPercentage"));
	}
	
	public static double getImageDPI() {
		return Double.parseDouble(props.getProperty("imageDPI"));
	}
	
	public static boolean renderSVG() {
		return Boolean.parseBoolean(props.getProperty("renderSVG"));
	}
}
