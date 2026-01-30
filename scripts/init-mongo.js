db = db.getSiblingDB('neocommercepay_products');

db.createCollection('products');
db.createCollection('categories');
db.createCollection('inventory');

db.products.createIndex({ name: "text", description: "text" });
db.products.createIndex({ categoryId: 1 });
db.products.createIndex({ price: 1 });

db.inventory.createIndex({ productId: 1 }, { unique: true });

db.categories.insertMany([
    {
        _id: ObjectId(),
        name: "Electronics",
        description: "Electronic devices and gadgets",
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId(),
        name: "Clothing",
        description: "Apparel and fashion items",
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId(),
        name: "Books",
        description: "Books and publications",
        createdAt: new Date(),
        updatedAt: new Date()
    }
]);

print("MongoDB collections and indexes created successfully");
