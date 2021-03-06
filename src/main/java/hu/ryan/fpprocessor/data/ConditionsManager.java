package hu.ryan.fpprocessor.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.exceptions.HdfException;
import io.jhdf.exceptions.HdfInvalidPathException;

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

	// check if condition is empty
	// check if path is real
	// check if path really leads to 2d array
	// check if elements point to all the nodes
	public boolean readMeshFile(File file, String nodePath, String elementPath, Condition condition) {
		HdfFile hdf;
		Dataset nodeData;
		Dataset elementData;
		try {
			hdf = new HdfFile(file);
			nodeData = hdf.getDatasetByPath(nodePath);
			elementData = hdf.getDatasetByPath(elementPath);
		} catch (HdfException e) {
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
				hdf.close();
				return false;
			}
		} catch (Exception e) {
			hdf.close();
			return false;
		}

		int maxNode = 0;
		int nodesSize = condition.getNodesSize();
		try {
			if (elementData.getDimensions()[1] == 4 && elementData.getJavaType().equals(int.class)) {
				for (int[] element : (int[][]) elementData.getData()) {
					Element temp = new Element();
					for (int nodePointer : element) {
						if (nodePointer != -1 && nodePointer -1 < nodesSize) {
							temp.addNode(condition.getNode(nodePointer - 1));
							maxNode = nodePointer - 1 > maxNode ? nodePointer - 1 : maxNode;
						}
					}
					condition.addElement(temp);
				}
				if (maxNode != nodesSize - 1) {
					hdf.close();
					return false;
				}
			} else {
				hdf.close();
				return false;
			}
		} catch (Exception e) {
			hdf.close();
			return false;
		}
		hdf.close();
		return true;
	}

	// check if each value is the right size
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
		} catch (HdfException e) {
			return false;
		}
		
		
		int timestamps;
		int i = 0;
		int j = 0;
		try {
			if (depthData.getDimensions()[1] == condition.nodesSize && depthData.getJavaType().equals(float.class)) {
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
				hdf.close();
				return false;
			}
		} catch (Exception e) {
			hdf.close();
			return false;
		}
		
		try {
			if (wSELData.getDimensions()[0] == timestamps && wSELData.getDimensions()[1] == condition.nodesSize
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
				hdf.close();
				return false;
			}
		} catch (Exception e) {
			hdf.close();
			return false;
		}

		i = 0;
		try {
			if (shearStressData.getDimensions()[0] == timestamps
					&& shearStressData.getDimensions()[1] == condition.nodesSize
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
				hdf.close();
				return false;
			}
		} catch (Exception e) {
			hdf.close();
			return false;
		}

		i = 0;
		try {
			if (velData.getDimensions()[0] == timestamps && velData.getDimensions()[1] == condition.nodesSize
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
				hdf.close();
				return false;
			}
		} catch (Exception e) {
			hdf.close();
			return false;
		}
		
		hdf.close();
		//System.out.println(condition);
		return true;
	}
	
	public void readGeorefFile(File file, Condition condition) throws IOException, NumberFormatException {
		double arr[];
		double width, height;
		// 0 = xScale, 1 = ySkew, 2 = xSkew, 3 = yScale, 4 = x, 5 = y
		arr = Files.lines(file.toPath()).mapToDouble(line -> Double.parseDouble(line)).toArray();
		BufferedImage img = ImageIO.read(condition.getMapImage());
		width = Math.sqrt(arr[0] * arr[0] + arr[1] * arr[1]) * img.getWidth();
		height = Math.sqrt(arr[2] * arr[2] + arr[3] * arr[3]) * img.getHeight();
		condition.setGeorefData(Arrays.asList(arr[4], arr[4] + width, arr[5] - height, arr[5]));
	}
}
