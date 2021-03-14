package fpprocessor.graphics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;

import fpprocessor.Config;
import fpprocessor.ProgramLogger;

public class PythonExecutor {

	private String pythonPath;
	private String scriptName;
	private StringBuilder commands;

	public PythonExecutor(String pythonPath, String scriptName) {
		this.pythonPath = pythonPath;
		this.scriptName = scriptName;
		commands = new StringBuilder();
	}

	public void addCommand(String command) {
		commands.append(command).append("\n");
	}

	public String run() throws IOException {
		// create output folder
		File saveDir = new File(Config.getLocalScriptPath());
		if (saveDir.mkdir()) {
			ProgramLogger.log(this.getClass(), ProgramLogger.INFO, "Created folder " + saveDir.getAbsolutePath());
		} else {
			ProgramLogger.log(this.getClass(), ProgramLogger.INFO, saveDir.getAbsolutePath() + " already exists");
		}

		// create temporary Python script
		File file = new File(Config.getLocalScriptPath() + scriptName + ".py");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		if (writeFile(file, commands.toString()) == false) {
			return null;
		}
		
		// create the ProcessBuilder
		ProcessBuilder pb = new ProcessBuilder(pythonPath + "python", Config.getLocalScriptPath() + file.getName());
		pb.redirectErrorStream(true);
		Process p = pb.start();
		ProgramLogger.log(getClass(), ProgramLogger.INFO, "Started Python Process: " + file.getAbsolutePath());
		
		// return the output
		StringBuilder result = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = "";
		while ((line = br.readLine()) != null) {
			result.append("Python Output: ").append(line).append("\n");
		}
		ProgramLogger.log(getClass(), ProgramLogger.INFO, "Finished Python Process: " + file.getAbsolutePath());
		return result.toString();
	}

	public boolean writeFile(File file, String string) {
		try (FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE)) {
			byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
			MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, bytes.length);
			buffer.put(bytes);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
