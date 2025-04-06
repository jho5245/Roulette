import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class AudioPlayer
{
	File file;

	AudioInputStream audioInputStream;

	Clip clip;

	public AudioPlayer(String fileName)
	{
		file = new File(fileName);
		try
		{
			audioInputStream = AudioSystem.getAudioInputStream(file);
			clip = AudioSystem.getClip();
			clip.open(audioInputStream);
		}
		catch (Throwable t)
		{
			System.out.println(t.getMessage());
		}
	}

	public void start()
	{
		clip.setMicrosecondPosition(0);
		clip.start();
	}

	public void stop()
	{
		clip.stop();
	}

	public void close()
	{
		clip.close();
	}
}
