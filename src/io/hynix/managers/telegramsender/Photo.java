package io.hynix.managers.telegramsender;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Photo {
    private static final List<String> photoList = new ArrayList<>();
    private static final Random random = new Random();

    static {
        photoList.add("https://i.pinimg.com/736x/99/1e/2e/991e2ed47b39736d06ddd809ed7eacc2.jpg");
        photoList.add("https://i.pinimg.com/736x/e8/fa/36/e8fa36874e983ffa81ab824121295776.jpg");
        photoList.add("https://i.pinimg.com/736x/00/8f/59/008f597ef40944d9fece0e6b07a8a876.jpg");
        photoList.add("https://i.pinimg.com/736x/e9/c9/93/e9c99388949073e2c4914b7b164c2e40.jpg");
        photoList.add("https://i.pinimg.com/736x/f6/29/0e/f6290e6119ffdfe617dd64a2f3744dda.jpg");
        photoList.add("https://i.pinimg.com/736x/e5/eb/45/e5eb455ae7290e7ab28c9a8dbd37a8bd.jpg");
        photoList.add("https://i.pinimg.com/736x/ac/0d/a2/ac0da29497d1c122a1dadf06eb334611.jpg");
    }

    public static String getRandomPhoto() {
        int index = random.nextInt(photoList.size());
        return photoList.get(index);
    }
}

