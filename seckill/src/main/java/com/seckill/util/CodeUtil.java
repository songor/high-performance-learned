package com.seckill.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CodeUtil {

    private static int width = 90;

    private static int height = 20;

    // 定义图片上显示的验证码个数
    private static int codeCount = 4;

    private static int codeX = 15;

    private static int codeY = 16;

    private static int fontHeight = 18;

    private static char[] codeSequence = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    public static Map<String, Object> generateCodeAndPic() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        Random random = new Random();

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);

        Font font = new Font("Fixedsys", Font.BOLD, fontHeight);
        graphics.setFont(font);

        graphics.setColor(Color.BLACK);
        graphics.drawRect(0, 0, width - 1, height - 1);

        // 干扰线
        graphics.setColor(Color.BLACK);
        for (int i = 0; i < 30; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int incrementX = random.nextInt(12);
            int incrementY = random.nextInt(12);
            graphics.drawLine(x, y, x + incrementX, y + incrementY);
        }

        StringBuffer code = new StringBuffer();
        int red = 0, green = 0, blue = 0;

        for (int i = 0; i < codeCount; i++) {
            String randomCode = String.valueOf(codeSequence[random.nextInt(36)]);
            red = random.nextInt(255);
            green = random.nextInt(255);
            blue = random.nextInt(255);

            graphics.setColor(new Color(red, green, blue));
            graphics.drawString(randomCode, (i + 1) * codeX, codeY);

            code.append(randomCode);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("codePic", image);
        return map;
    }

    public static void main(String[] args) throws Exception {
        OutputStream out = new FileOutputStream(System.currentTimeMillis() + ".jpg");
        Map<String, Object> map = CodeUtil.generateCodeAndPic();
        ImageIO.write((RenderedImage) map.get("codePic"), "jpeg", out);
        System.out.println("验证码为：" + map.get("code"));
    }

}
