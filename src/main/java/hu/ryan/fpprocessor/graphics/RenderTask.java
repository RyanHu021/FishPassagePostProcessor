package hu.ryan.fpprocessor.graphics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.ryan.fpprocessor.Config;
import hu.ryan.fpprocessor.ProgramLogger;
import hu.ryan.fpprocessor.data.Element;
import hu.ryan.fpprocessor.data.Node;

public class RenderTask {

	public Map<String, File> render(List<RenderItem> renderQueue) throws IOException {
		if (renderQueue.isEmpty()) {
			return null;
		}
		Map<String, File> results = new HashMap<String, File>();
		int size = results.size();
		RenderItem current;
		PythonExecutor py;

		// create output folders
		File saveDir = new File(Config.getLocalRenderPath());
		File saveDirSVG = new File(Config.getLocalRenderPath() + "svg/");
		if (saveDir.mkdir()) {
			ProgramLogger.log(this.getClass(), ProgramLogger.INFO, "Created folder " + saveDir.getAbsolutePath());
		} else {
			ProgramLogger.log(this.getClass(), ProgramLogger.INFO, saveDir.getAbsolutePath() + " already exists");
		}
		if (saveDirSVG.mkdir()) {
			ProgramLogger.log(this.getClass(), ProgramLogger.INFO, "Created folder " + saveDirSVG.getAbsolutePath());
		} else {
			ProgramLogger.log(this.getClass(), ProgramLogger.INFO, saveDirSVG.getAbsolutePath() + " already exists");
		}

		// while there are still RenderItems in the queue
		while (!renderQueue.isEmpty()) {
			current = renderQueue.get(0);
			
			// create and set up a Python script
			py = new PythonExecutor(Config.getPythonPath(),
					current.getCondition().getName() + "_" + current.getTimestamp() + "_" + current.getProperty());
			py.addCommand("import numpy as np");
			py.addCommand("import matplotlib.pyplot as plt");
			py.addCommand("x = []");
			py.addCommand("y = []");
			py.addCommand("z = []");

			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			
			double xMin = Double.MAX_VALUE;
			double xMax = Double.MIN_VALUE;
			double yMin = Double.MAX_VALUE;
			double yMax = Double.MIN_VALUE;

			// append all nodes of the selected property into list z
			for (Node node : current.getCondition().getNodes()) {
				double d;
				switch (current.getProperty()) {
				case DEPTH:
					d = node.getTimestamp(current.getTimestamp()).getDepth();
					break;
				case WSEL:
					d = node.getTimestamp(current.getTimestamp()).getWSEL();
					break;
				case SHEAR_STRESS:
					d = node.getTimestamp(current.getTimestamp()).getShearStress();
					break;
				default:
					d = node.getTimestamp(current.getTimestamp()).getVelocity().getX() == -999.0 ? -999.0
							: node.getTimestamp(current.getTimestamp()).getVelocity().getMagnitude();
				}

				// find the minimum and maximum values
				if (d != -999) {
					min = Math.min(min, d);
					max = Math.max(max, d);
				}
				xMin = Math.min(xMin, node.getX());
				xMax = Math.max(xMax, node.getX());
				yMin = Math.min(yMin, node.getY());
				yMax = Math.max(yMax, node.getY());
				
				
				// append the values to their respective lists
				py.addCommand("x.append(" + node.getX() + ")");
				py.addCommand("y.append(" + node.getY() + ")");
				py.addCommand("z.append(" + d + ")");
			}

			String trianglesParam = "";

			// use the nodes linked to each element to triangulate 4-sided elements
			if (Config.useElementBasedTriangulation()) {
				py.addCommand("triangles = []");
				trianglesParam = "triangles, ";
				for (Element element : current.getCondition().getElements()) {
					List<Node> nodes = element.getNodes();
					py.addCommand("triangles.append([" + nodes.get(0).getID() + ", " + nodes.get(1).getID() + ", "
							+ nodes.get(2).getID() + "])");
					if (nodes.size() == 4) {
						py.addCommand("triangles.append([" + nodes.get(0).getID() + ", " + nodes.get(2).getID() + ", "
								+ nodes.get(3).getID() + "])");
					}
				}
			}

			// add map image
			if (current.getCondition().hasImage()) {
				File to = new File("map_" + current.getCondition().getName() + "_" + current.getTimestamp() + "_"
						+ current.getProperty() + current.getCondition().getMapImage().getName()
								.substring(current.getCondition().getMapImage().getName().lastIndexOf(".")));
				Files.copy(current.getCondition().getMapImage().toPath(), to.toPath(),
						StandardCopyOption.REPLACE_EXISTING);
				py.addCommand("plt.imshow(plt.imread('" + to.getName() + "'), extent="
						+ current.getCondition().getGeorefData() + ")");
				to.deleteOnExit();
			}

			// create tricontour and tricontourf plots and color bar
			py.addCommand("lvlf = np.linspace(" + min + ", " + max + ", " + Config.getContourFillLevels() + ")");
			py.addCommand("lvl = np.linspace(" + min + ", " + max + ", " + Config.getContourLineLevels() + ")");
			py.addCommand("tcf = plt.tricontourf(x, y, " + trianglesParam + "z, levels=lvlf, cmap='"
					+ Config.getContourColorMap() + "', vmin=" + min + ", vmax=" + max + ")");
			py.addCommand("cbar = plt.colorbar(tcf, ticks=lvl, shrink=" + Config.getColorBarScale() + ")");
			py.addCommand("tc = plt.tricontour(x, y, " + trianglesParam + "z, levels=lvl, colors='k', linewidths="
					+ Config.getContourLineWidth() + ")");
			py.addCommand("cbar.add_lines(tc)");
			py.addCommand("plt.subplots_adjust(top = 1, bottom = 0, right = 1, left = 0, hspace = 0, wspace = 0)");
			
			// crop the plot
			double left = xMin - ((xMax - xMin) * Config.getPlotPaddingPercentage());
			double right = xMax + ((xMax - xMin) * Config.getPlotPaddingPercentage());
			double bottom = yMin - ((yMax - yMin) * Config.getPlotPaddingPercentage());
			double top = yMax + ((yMax - yMin) * Config.getPlotPaddingPercentage());
			py.addCommand("ax = plt.gca()");
			py.addCommand("ax.set_xlim([ + " + left + ", " + right + "])");
			py.addCommand("ax.set_ylim([ + " + bottom + ", " + top + "])");
			py.addCommand("ax.set_xticks([])");
			py.addCommand("ax.set_yticks([])");
			
			// add title to plot
			if (current.getTitle() != null) {
				py.addCommand("plt.title('" + current.getTitle() + "')");
			}

			// save rendered images
			File result = new File(Config.getLocalRenderPath() + "plot_" + current.getCondition().getName() + "_"
					+ current.getTimestamp() + "_" + current.getProperty() + ".png");
			py.addCommand("plt.savefig('" + Config.getLocalRenderPath() + result.getName() + "', dpi="
					+ Config.getImageDPI() + ", bbox_inches='tight', pad_inches=0)");
			if (Config.renderSVG()) {
				File resultSVG = new File(Config.getLocalRenderPath() + "svg/plot_" + current.getCondition().getName()
						+ "_" + current.getTimestamp() + "_" + current.getProperty() + ".svg");
				py.addCommand("plt.savefig('" + Config.getLocalRenderPath() + "svg/" + resultSVG.getName() + "')");
			}

			// adds the resulting image to the return list
			if (current.getTitle() != null) {
				results.put(current.getTitle(), result);
			}

			// show if there is only one RenderItem
			System.out.println(size);
			if (size == 1) {
				py.addCommand("plt.show()");
			}
			String pyOut = py.run();
			System.out.println(pyOut);
			renderQueue.remove(0);
		}

		return results;
	}
}
