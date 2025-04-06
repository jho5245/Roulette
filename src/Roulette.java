import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.*;

public class Roulette
{
	// 룰렛 연출 최대 시간(5초)
	public static final int MAX_ROLL_TIME = 5000;

	// 룰렛 연출에 사용할 최대 이미지 수(100개)
	public static final int MAX_ROLL_IMAGE = 100;

	public static final String SOUND_ROLL = "roll.wav", SOUND_RESULT = "result.wav";

	private final int width;

	private final int height;

	private final JFrame window;

	// rollingImage = 연출용 합성 이미지
	private ImageIcon backgroundImage, buttonImage, resultImage, initImage, rollingImage;

	private JButton rollButton;

	private List<ImageIcon> icons;

	private final List<ImageIcon> done = new ArrayList<>();

	private boolean isRolling = false;

	private final AudioPlayer roll, result;

	private JLabel backgroundLabel, resultLabel, rollingLabel;

	private JPanel buttonPanel;

	private int rollingImageY = 0;

	public Roulette(String title, int width, int height)
	{
		this.width = width;
		this.height = height;
		window = new JFrame(title);
		window.setBounds(0, 0, width + 16, height + 39);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocationRelativeTo(null);
		window.setResizable(false);
		window.setLayout(null);

		roll = new AudioPlayer("data/" + SOUND_ROLL);
		result = new AudioPlayer("data/" + SOUND_RESULT);

		loadImages();
		addResources();

		window.setVisible(true);
	}

	private void addResources()
	{
		System.out.println("화면에 요소 추가하는 중..");

		backgroundLabel.setLayout(null);
		backgroundLabel.setSize(width, height);
		window.add(backgroundLabel);

		rollingLabel.setLayout(null);
		rollingLabel.setSize(width, height);
		rollingLabel.setVisible(false);
		window.add(rollingLabel);

		resultLabel.setLayout(null);
		resultLabel.setBounds(width / 8, height / 4, resultImage.getIconWidth(), resultImage.getIconHeight());
		window.add(resultLabel);

		rollButton.setBounds(0, 0, 90 + 13, 40);
		rollButton.setFocusPainted(false);
		rollButton.setBorder(null);
		rollButton.setIcon(buttonImage);
		rollButton.setActionCommand("roll");
		rollButton.addActionListener(new ButtonHandler(this));
		buttonPanel.setLayout(null);
		buttonPanel.setBounds(width / 2 - rollButton.getWidth() / 2, height * 4 / 5, buttonImage.getIconWidth(), buttonImage.getIconHeight());
		buttonPanel.add(rollButton);
		window.add(buttonPanel);

		System.out.println("화면에 요소 추가 완료!");
	}

	private void loadImages()
	{
		System.out.println("이미지를 불러오는 중..");

		backgroundImage = new ImageIcon("data/Background.png");
		rollingImage = new ImageIcon("data/rolling.png");
		initImage = new ImageIcon("data/init_result.png");
		resultImage = new ImageIcon("data/init_result.png");
		buttonImage = new ImageIcon("data/Button.png");

		backgroundLabel = new JLabel(backgroundImage);
		rollingLabel = new JLabel(rollingImage);
		resultLabel = new JLabel(resultImage);
		rollButton = new JButton("button");
		buttonPanel = new JPanel();

		File dataFolder = new File("data");
		if (!dataFolder.exists() || !dataFolder.isDirectory())
		{
			System.out.println("data 폴더가 없습니다!");
			System.exit(0);
		}

		File folder = new File("data/Images");
		if (!folder.exists())
		{
			System.out.println("Images 폴더가 없습니다!");
			System.exit(0);
		}
		File[] files = folder.listFiles();
		if (files == null)
		{
			System.out.println("Images 폴더가 없습니다!");
			System.exit(0);
		}
		icons = new ArrayList<>();
		for (File file : files)
		{
			String fileName = file.getName();
			if (fileName.endsWith("png") || fileName.endsWith("gif"))
			{
				icons.add(new ImageIcon(file.getAbsolutePath()));
			}
		}
		if (icons.isEmpty())
		{
			System.out.println("Images 폴더에 이미지 파일이 하나도 없습니다!");
			System.exit(0);
		}
		System.out.printf("이미지 파일 %s개 로드됨.\n", icons.size());
		System.out.println("룰렛 연출용 이미지 생성중..");
		makeRollingImage(icons);
		System.out.println("룰렛 연출용 이미지 생성 완료!");
	}

	private static class ButtonHandler implements ActionListener
	{
		private final Roulette instance;

		public ButtonHandler(Roulette roulette)
		{
			instance = roulette;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (e.getActionCommand().equals("roll"))
			{
				if (instance.isRolling)
				{
					System.out.println("아직 돌리는 중입니다!");
					return;
				}
				if (instance.done.size() == instance.icons.size())
				{
					System.out.println("이미 다 뽑아서 초기화합니다!");
					instance.resultImage.setImage(instance.initImage.getImage());
					instance.window.repaint();
					instance.done.clear();
					return;
				}
				System.out.println("돌리는 중!");

				List<ImageIcon> imageIcons = instance.getAvailableImages();
				int height = instance.resultImage.getIconHeight() * Math.min(MAX_ROLL_IMAGE, imageIcons.size());
				instance.isRolling = true;

				// 소리 재생
				Timer rollingSoundTimer = new Timer();
				rollingSoundTimer.schedule(new TimerTask()
				{
					@Override
					public void run()
					{
						instance.roll.start();
					}
				}, 0L, 200L);

				// 이미지 활성화
				instance.rollingLabel.setVisible(true);
				instance.resultLabel.setVisible(false);

				// 이미지 위로 이동 연출
				Timer rollingImageTimer = new Timer();
				rollingImageTimer.schedule(new TimerTask()
				{
					@Override
					public void run()
					{
						instance.rollingImageY -= 10;

						instance.rollingLabel.setBounds(instance.width / 8, instance.rollingImageY + instance.height / 10, instance.resultImage.getIconWidth(), height);
//						instance.window.repaint();
					}
				}, 0L, 5L);

				new Timer().schedule(new TimerTask()
				{
					@Override
					public void run()
					{
						rollingSoundTimer.cancel();
						rollingImageTimer.cancel();
						instance.rollingImageY = 0;
						instance.rollingLabel.setVisible(false);
						instance.resultLabel.setVisible(true);
						ImageIcon result = instance.getResult();
						instance.done.add(result);
						instance.resultImage.setImage(result.getImage());
//						instance.window.repaint();
						instance.isRolling = false;
						System.out.printf("결과가 나왔습니다! (%s개 남음)\n", (instance.icons.size() - instance.done.size()));
						instance.result.start();
						instance.makeRollingImage(imageIcons);
					}
				}, Math.min(MAX_ROLL_TIME, (instance.icons.size() - instance.done.size()) * 50L));
			}
		}
	}

	// 랜덤 결과 이미지 뽑기
	private ImageIcon getResult()
	{
		List<ImageIcon> list = getAvailableImages();
		return list.get((int) (list.size() * Math.random()));
	}

	// 이미 뽑은거 제외한 나머지 이미지들
	private List<ImageIcon> getAvailableImages()
	{
		List<ImageIcon> list = new ArrayList<>(icons);
		list.removeAll(done);
		return list;
	}

	// 연출용 이미지 생성
	private void makeRollingImage(List<ImageIcon> imageIcons)
	{
		List<ImageIcon> icons = new ArrayList<>(imageIcons);
		// rolling 이미지 합성하여 저장후 사용
		while (icons.size() >= MAX_ROLL_IMAGE)
		{
			icons.remove(icons.get((int) (Math.random() * icons.size() - 1)));
		}

		// 이미지 순서 섞기
		Collections.shuffle(icons);

		int height = resultImage.getIconHeight();
		BufferedImage mergedImage = new BufferedImage(resultImage.getIconWidth(), height * icons.size(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) mergedImage.getGraphics();
		g.setBackground(Color.white);
		for (int i = 0; i < icons.size(); i++)
		{
			g.drawImage(icons.get(i).getImage(), 0, i * height, null);
		}
		try
		{
			File file = new File("data/rolling.png");
			ImageIO.write(mergedImage, "png", file);
			rollingImage.getImage().flush();
			rollingImage = new ImageIcon("data/rolling.png");
			rollingLabel.setIcon(rollingImage);
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}
}
