package com.dvbakes.config;

import com.dvbakes.entity.Product;
import com.dvbakes.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        long count = productRepository.count();
        if (count == 0) {
            log.info("Seeding initial product catalog into database...");
            productRepository.saveAll(getInitialProducts());
            log.info("Product seeding complete! {} products added.", productRepository.count());
        } else {
            log.info("Database already has {} products. Skipping seed.", count);
        }
    }

    private List<Product> getInitialProducts() {
        return List.of(
            Product.builder().id("choco-bliss").title("Velvet Choco Bliss").category("Signature Shake")
                .price(6.99).stock(12).src("./images/choco.png").alt("Chocolate Shake").bgText("CHOCO")
                .description("Dive into layers of rich cocoa, smooth cream, and a swirl of happiness.")
                .bg("radial-gradient(circle at center, #ffffff 0%, #f3ebd8 50%, #c49675 100%)")
                .themeColor("#5c2e1a").accentColor("#a1673f").textColor("#3a2b23")
                .specs("[{\"label\":\"Rich Cocoa\",\"value\":\"85%\"},{\"label\":\"Milk Type\",\"value\":\"Oat Milk\"},{\"label\":\"Calories\",\"value\":\"320 kcal\"},{\"label\":\"Serving\",\"value\":\"350 ml\"}]")
                .ingredients("[\"Dutch Cocoa Powder\",\"Belgian Chocolate Chunks\",\"Organic Oat Milk\",\"Brown Sugar Cane\",\"Gourmet Whipped Cream\"]")
                .nutrition("[{\"name\":\"Carbs\",\"percentage\":65},{\"name\":\"Fats\",\"percentage\":45},{\"name\":\"Proteins\",\"percentage\":35},{\"name\":\"Sugars\",\"percentage\":25}]")
                .createdAt(java.time.Instant.now().toString()).active(true).build(),

            Product.builder().id("mint-cupcake").title("Minty Cupcake Cloud").category("Gourmet Cupcake")
                .price(4.50).stock(8).src("./images/cupcake.png").alt("Cupcake").bgText("SWEET")
                .description("A swirl of vanilla, a dash of mint, and the fluffiest cupcake you have ever met.")
                .bg("radial-gradient(circle at center, #ffffff 0%, #e6f7ef 50%, #abd8c0 100%)")
                .themeColor("#1c4c34").accentColor("#3a9b6c").textColor("#1a3327")
                .specs("[{\"label\":\"Gluten-free\",\"value\":\"No\"},{\"label\":\"Frosting\",\"value\":\"Mint Buttercream\"},{\"label\":\"Calories\",\"value\":\"240 kcal\"},{\"label\":\"Bake Temp\",\"value\":\"180°C\"}]")
                .ingredients("[\"Organic Wheat Flour\",\"Madagascar Vanilla Extract\",\"Fresh Mint Leaves\",\"Swiss Meringue Buttercream\",\"Sugar Pearls\"]")
                .nutrition("[{\"name\":\"Carbs\",\"percentage\":70},{\"name\":\"Fats\",\"percentage\":38},{\"name\":\"Proteins\",\"percentage\":12},{\"name\":\"Sugars\",\"percentage\":55}]")
                .createdAt(java.time.Instant.now().toString()).active(true).build(),

            Product.builder().id("blueberry-dream").title("Berrylicious Dream").category("Fresh Berry Shake")
                .price(7.25).stock(15).src("./images/blueberry.png").alt("Blueberry Shake").bgText("BERRY")
                .description("Bursting with real wild blueberries, whipped cream, and a whole lot of magic.")
                .bg("radial-gradient(circle at center, #ffffff 0%, #f4e8fa 50%, #caa4db 100%)")
                .themeColor("#4f2b5c").accentColor("#8a4c9c").textColor("#361d3f")
                .specs("[{\"label\":\"Berries\",\"value\":\"Wild Organic\"},{\"label\":\"Milk Type\",\"value\":\"Almond Milk\"},{\"label\":\"Calories\",\"value\":\"290 kcal\"},{\"label\":\"Serving\",\"value\":\"350 ml\"}]")
                .ingredients("[\"Wild Organic Blueberries\",\"Unsweetened Almond Milk\",\"Raw Clover Honey\",\"Chia Seeds\",\"Greek Vanilla Yogurt\"]")
                .nutrition("[{\"name\":\"Carbs\",\"percentage\":50},{\"name\":\"Fats\",\"percentage\":22},{\"name\":\"Proteins\",\"percentage\":40},{\"name\":\"Sugars\",\"percentage\":18}]")
                .createdAt(java.time.Instant.now().toString()).active(true).build(),

            Product.builder().id("strawberry-donut").title("Sugar Glazed Hug").category("Handcrafted Donut")
                .price(3.99).stock(20).src("./images/donut.png").alt("Donut").bgText("DONUT")
                .description("Soft, fluffy, and coated in pink sweetness. A cuddle disguised as a delicious snack.")
                .bg("radial-gradient(circle at center, #ffffff 0%, #fdf0f4 50%, #f3b5c7 100%)")
                .themeColor("#7a2f45").accentColor("#d65376").textColor("#421a25")
                .specs("[{\"label\":\"Glaze\",\"value\":\"Strawberry Glaze\"},{\"label\":\"Baked Type\",\"value\":\"Fluffy Yeast\"},{\"label\":\"Calories\",\"value\":\"190 kcal\"},{\"label\":\"Freshness\",\"value\":\"24 Hours\"}]")
                .ingredients("[\"Wheat Flour Blend\",\"Active Yeast\",\"Strawberry Nectar Glaze\",\"Rainbow Sugar Sprinkles\",\"Vanilla Extract\"]")
                .nutrition("[{\"name\":\"Carbs\",\"percentage\":75},{\"name\":\"Fats\",\"percentage\":48},{\"name\":\"Proteins\",\"percentage\":15},{\"name\":\"Sugars\",\"percentage\":40}]")
                .createdAt(java.time.Instant.now().toString()).active(true).build(),

            Product.builder().id("golden-croissant").title("Golden Glaze Croissant").category("Warm Pastry")
                .price(3.49).stock(5).src("./images/croissant.png").alt("Golden Croissant").bgText("PASTRY")
                .description("Flaky, buttery layers baked to golden perfection. Every bite releases a warm, rich aroma.")
                .bg("radial-gradient(circle at center, #ffffff 0%, #fef3e2 50%, #f1c40f 100%)")
                .themeColor("#7e5109").accentColor("#d4ac0d").textColor("#4a3002")
                .specs("[{\"label\":\"Butter\",\"value\":\"Normandy 82%\"},{\"label\":\"Style\",\"value\":\"French Classic\"},{\"label\":\"Calories\",\"value\":\"280 kcal\"},{\"label\":\"Serving\",\"value\":\"90 g\"}]")
                .ingredients("[\"Stone Ground Flour\",\"French Normandy Butter\",\"Dry Active Yeast\",\"Sea Salt\",\"Organic Sugar Cane\"]")
                .nutrition("[{\"name\":\"Carbs\",\"percentage\":45},{\"name\":\"Fats\",\"percentage\":65},{\"name\":\"Proteins\",\"percentage\":20},{\"name\":\"Sugars\",\"percentage\":8}]")
                .createdAt(java.time.Instant.now().toString()).active(true).build(),

            Product.builder().id("red-velvet-cake").title("Red Velvet Symphony").category("Gourmet Cake")
                .price(5.99).stock(6).src("./images/cake.png").alt("Red Velvet Cake").bgText("CAKE")
                .description("Layers of vibrant red cocoa sponge paired with silky cream cheese frosting.")
                .bg("radial-gradient(circle at center, #ffffff 0%, #fdebd0 50%, #e74c3c 100%)")
                .themeColor("#78281f").accentColor("#c0392b").textColor("#4d1c17")
                .specs("[{\"label\":\"Layers\",\"value\":\"3 Tier Sponge\"},{\"label\":\"Frosting\",\"value\":\"Cream Cheese\"},{\"label\":\"Calories\",\"value\":\"380 kcal\"},{\"label\":\"Bake Temp\",\"value\":\"175°C\"}]")
                .ingredients("[\"Cake Flour\",\"Premium Cocoa Powder\",\"Cream Cheese\",\"Organic Buttermilk\",\"Vanilla Extract\"]")
                .nutrition("[{\"name\":\"Carbs\",\"percentage\":68},{\"name\":\"Fats\",\"percentage\":52},{\"name\":\"Proteins\",\"percentage\":18},{\"name\":\"Sugars\",\"percentage\":48}]")
                .createdAt(java.time.Instant.now().toString()).active(true).build(),

            Product.builder().id("pistachio-macaron").title("Pistachio Dream Macaron").category("French Macaron")
                .price(4.99).stock(30).src("./images/macaron.png").alt("Pistachio Macarons").bgText("SWEET")
                .description("Delicate almond meringue shells filled with rich, nutty pistachio white chocolate ganache.")
                .bg("radial-gradient(circle at center, #ffffff 0%, #ebf5fb 50%, #85c1e9 100%)")
                .themeColor("#1b4f72").accentColor("#3498db").textColor("#153e5b")
                .specs("[{\"label\":\"Flour\",\"value\":\"Almond Meal\"},{\"label\":\"Filling\",\"value\":\"Pistachio Ganache\"},{\"label\":\"Calories\",\"value\":\"150 kcal\"},{\"label\":\"Gluten-free\",\"value\":\"Yes\"}]")
                .ingredients("[\"Almond Flour\",\"Egg Whites\",\"Powdered Sugar\",\"Roasted Pistachios\",\"White Chocolate\"]")
                .nutrition("[{\"name\":\"Carbs\",\"percentage\":38},{\"name\":\"Fats\",\"percentage\":42},{\"name\":\"Proteins\",\"percentage\":24},{\"name\":\"Sugars\",\"percentage\":30}]")
                .createdAt(java.time.Instant.now().toString()).active(true).build(),

            Product.builder().id("lava-cake").title("Gourmet Fudge Lava Cake").category("Gourmet Cake")
                .price(6.25).stock(8).src("./images/lava_cake.png").alt("Gourmet Fudge Lava Cake").bgText("LAVA")
                .description("A rich chocolate lava cake with warm molten Belgian fudge flowing from the center.")
                .bg("radial-gradient(circle at center, #ffffff 0%, #f3ebd8 50%, #c49675 100%)")
                .themeColor("#5c2e1a").accentColor("#a1673f").textColor("#3a2b23")
                .specs("[{\"label\":\"Fudge\",\"value\":\"Belgian Molten\"},{\"label\":\"Layers\",\"value\":\"Warm Liquid Center\"},{\"label\":\"Calories\",\"value\":\"390 kcal\"},{\"label\":\"Serving\",\"value\":\"120 g\"}]")
                .ingredients("[\"Belgian Cocoa Liquor\",\"Brown Butter\",\"Gourmet Sugar Cane\",\"Fresh Farm Eggs\",\"Vanilla Bean Powder\"]")
                .nutrition("[{\"name\":\"Carbs\",\"percentage\":62},{\"name\":\"Fats\",\"percentage\":56},{\"name\":\"Proteins\",\"percentage\":18},{\"name\":\"Sugars\",\"percentage\":42}]")
                .createdAt(java.time.Instant.now().toString()).active(true).build()
        );
    }
}
