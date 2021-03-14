package fpprocessor.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import fpprocessor.ProgramLogger;
import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.exceptions.HdfException;

public class ConditionsManager {

	private Map<String, Condition> conditions;

	public ConditionsManager() {
		conditions = new HashMap<String, Condition>();
	}

	public boolean addCondition(String name, Condition condition) {
		if (conditions.containsKey(name)) {
			return false;
		}
		conditions.put(name, condition);
		return true;
	}

	public Condition getCondition(String name) {
		if (!conditions.containsKey(name)) {
			return null;
		}
		return conditions.get(name);
	}

	public void removeCondition(String name) {
		conditions.remove(name);
	}

	public boolean readMeshFile(File file, String nodePath, String elementPath, Condition condition) {
		HdfFile hdf;
		Dataset nodeData;
		Dataset elementData;
		try {
			hdf = new HdfFile(file);
			nodeData = hdf.getDatasetByPath(nodePath);
			elementData = hdf.getDatasetByPath(elementPath);
			ProgramLogger.log(getClass(), ProgramLogger.INFO, "Opened mesh file: " + file.getAbsolutePath());
		} catch (HdfException e) {
			ProgramLogger.log(getClass(), ProgramLogger.ERROR, "Unable to open mesh file: " + file.getAbsolutePath());
			return false;
		}

		try {
			if (nodeData.getDimensions()[1] == 3 && nodeData.getJavaType().equals(double.class)) {
				int id = 0;
				for (double[] node : (double[][]) nodeData.getData()) {
					condition.addNode(new Node(id, node[0], node[1], node[2]));
					id++;
				}
			} else {
				ProgramLogger.log(getClass(), ProgramLogger.ERROR, "Unable to read nodes at path: " + nodePath);
				hdf.close();
				return false;
			}
		} catch (Exception e) {
			ProgramLogger.log(getClass(), ProgramLogger.ERROR, "Unable to read nodes at path: " + nodePath);
			hdf.close();
			return false;
		}
		ProgramLogger.log(getClass(), ProgramLogger.INFO,
				condition.getNodesSize() + " nodes read at path: " + nodePath);

		int maxNode = 0;
		int nodesSize = condition.getNodesSize();
		try {
			if (elementData.getDimensions()[1] == 4 && elementData.getJavaType().equals(int.class)) {
				for (int[] element : (int[][]) elementData.getData()) {
					Element temp = new Element();
					for (int nodePointer : element) {
						if (nodePointer != -1 && nodePointer - 1 < nodesSize) {
							temp.addNode(condition.getNode(nodePointer - 1));
							maxNode = nodePointer - 1 > maxNode ? nodePointer - 1 : maxNode;
						}
					}
					condition.addElement(temp);
				}
				if (maxNode != nodesSize - 1) {
					ProgramLogger.log(getClass(), ProgramLogger.ERROR, "Unable to read elements at path: " + nodePath);
					hdf.close();
					return false;
				}
			} else {
				ProgramLogger.log(getClass(), ProgramLogger.ERROR, "Unable to read elements at path: " + nodePath);
				hdf.close();
				return false;
			}
		} catch (Exception e) {
			ProgramLogger.log(getClass(), ProgramLogger.ERROR, "Unable to read elements at path: " + nodePath);
			hdf.close();
			return false;
		}
		ProgramLogger.log(getClass(), ProgramLogger.INFO,
				condition.getElementsSize() + " elements read at path: " + nodePath);
		hdf.close();
		return true;
	}

	public boolean readDataFile(File file, String depthPath, String wSELPath, String shearStressPath, String velPath,
			Condition condition) {
		HdfFile hdf;
		Dataset depthData;
		Dataset wSELData;
		Dataset shearStressData;
		Dataset velData;
		try {
			hdf = new HdfFile(file);
			depthData = hdf.getDatasetByPath(depthPath);
			wSELData = hdf.getDatasetByPath(wSELPath);
			shearStressData = hdf.getDatasetByPath(shearStressPath);
			velData = hdf.getDatasetByPath(velPath);
			ProgramLogger.log(getClass(), ProgramLogger.INFO, "Opened data file: " + file.getAbsolutePath());
		} catch (HdfException e) {
			ProgramLogger.log(getClass(), ProgramLogger.ERROR, "Unable to open data file: " + file.getAbsolutePath());
			return false;
		}

		int timestamps;
		int i = 0;
		int j = 0;
		try {
			if (depthData.getDimensions()[1] == condition.getNodesSize()
					&& depthData.getJavaType().equals(float.class)) {
				timestamps = depthData.getDimensions()[0];
				for (float[] timestamp : (float[][]) depthData.getData()) {
					for (double value : timestamp) {
						Timestamp temp = new Timestamp();
						temp.setDepth(value);
						condition.getNode(j).addTimestamp(temp);
						j++;
					}
					j = 0;
				}
			} else {
				ProgramLogger.log(getClass(), ProgramLogger.ERROR, "Unable to read depth at path: " + depthPath);
				hdf.close();
				return false;
			}
		} catch (Exception e) {
			ProgramLogger.log(getClass(), ProgramLogger.ERROR, "Unable to read depth at path: " + depthPath);
			hdf.close();
			return false;
		}
		ProgramLogger.log(getClass(), ProgramLogger.INFO, "Depth read for " + timestamps + " timestamps at path: " + depthPath);

		try {
			if (wSELData.getDimensions()[0] == timestamps && wSELData.getDimensions()[1] == condition.getNodesSize()
					&& wSELData.getJavaType().equals(float.class)) {
				for (float[] timestamp : (float[][]) wSELData.getData()) {
					for (double value : timestamp) {
						condition.getNode(j).getTimestamp(i).setWSEL(value);
						j++;
					}
					j = 0;
					i++;
				}
			} else {
				ProgramLogger.log(getClass(), ProgramLogger.ERROR, "Unable to read WSEL at path: " + wSELPath);
				hdf.close();
				return false;
			}
		} catch (Exception e) {
			ProgramLogger.log(getClass(), ProgramLogger.ERROR, "Unable to read WSEL at path: " + wSELPath);
			hdf.close();
			return false;
		}
		ProgramLogger.log(getClass(), ProgramLogger.INFO, "WSEL read for " + timestamps + " timestamps at path: " + wSELPath);

		i = 0;
		try {
			if (shearStressData.getDimensions()[0] == timestamps
					&& shearStressData.getDimensions()[1] == condition.getNodesSize()
					&& shearStressData.getJavaType().equals(float.class)) {
				for (float[] timestamp : (float[][]) shearStressData.getData()) {
					for (double value : timestamp) {
						condition.getNode(j).getTimestamp(i).setShearStress(value);
						j++;
					}
					j = 0;
					i++;
				}
			} else {
				ProgramLogger.log(getClass(), ProgramLogger.ERROR, "Unable to read shear stress at path: " + shearStressPath);
				hdf.close();
				return false;
			}
		} catch (Exception e) {
			ProgramLogger.log(getClass(), ProgramLogger.ERROR, "Unable to read shear stress at path: " + shearStressPath);
			hdf.close();
			return false;
		}
		ProgramLogger.log(getClass(), ProgramLogger.INFO, "Shear stress read for " + timestamps + " timestamps at path: " + shearStressPath);

		i = 0;
		try {
			if (velData.getDimensions()[0] == timestamps && velData.getDimensions()[1] == condition.getNodesSize()
					&& velData.getDimensions()[2] == 2 && velData.getJavaType().equals(float.class)) {
				for (float[][] timestamp : (float[][][]) velData.getData()) {
					for (float[] value : timestamp) {
						condition.getNode(j).getTimestamp(i).setVelocity(new Vector2D(value[0], value[1]));
						j++;
					}
					j = 0;
					i++;
				}
			} else {
				ProgramLogger.log(getClass(), ProgramLogger.ERROR, "Unable to read velocity at path: " + velPath);
				hdf.close();
				return false;
			}
		} catch (Exception e) {
			ProgramLogger.log(getClass(), ProgramLogger.ERROR, "Unable to read velocity at path: " + velPath);
			hdf.close();
			return false;
		}
		ProgramLogger.log(getClass(), ProgramLogger.INFO, "Velocity read for " + timestamps + " timestamps at path: " + velPath);

		hdf.close();
		return true;
	}

	public void readGeorefFile(File file, Condition condition) throws IOException, NumberFormatException {
		double[] arr;
		double width, height;
		try (Stream<String> stream = Files.lines(file.toPath())) {
			// 0 = xScale, 1 = ySkew, 2 = xSkew, 3 = yScale, 4 = x, 5 = y
			arr = stream.mapToDouble(line -> Double.parseDouble(line)).toArray();
			BufferedImage img = ImageIO.read(condition.getMapImage());
			width = Math.sqrt(arr[0] * arr[0] + arr[1] * arr[1]) * img.getWidth();
			height = Math.sqrt(arr[2] * arr[2] + arr[3] * arr[3]) * img.getHeight();
			condition.setGeorefData(Arrays.asList(arr[4], arr[4] + width, arr[5] - height, arr[5]));
		} catch (Exception e) {
			ProgramLogger.log(getClass(), ProgramLogger.ERROR, "Unable to read world file: " + file.getAbsolutePath());
			return;
		}
		ProgramLogger.log(getClass(), ProgramLogger.INFO, "Read world file: " + file.getAbsolutePath());
	}
}
