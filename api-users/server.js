const express = require('express');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

let users = [];

// 🟢 Crear usuario
app.post('/users', (req, res) => {
  console.log("📩 POST /users ->", req.body);

  const { name, email } = req.body;
  if (!name || !email) {
    return res.status(400).json({ message: 'Name and email are required' });
  }

  const newUser = { id: users.length + 1, name, email };
  users.push(newUser);
  console.log("✅ User added:", newUser);
  res.status(201).json(newUser);
});

// 🟡 Obtener todos los usuarios
app.get('/users', (req, res) => {
  console.log("📋 GET /users -> total:", users.length);
  res.json(users);
});

// 🔴 Eliminar usuario por ID
app.delete('/users/:id', (req, res) => {
  const userId = parseInt(req.params.id);
  console.log("🧾 DELETE /users -> id:", userId);

  const index = users.findIndex(u => u.id === userId);

  if (index === -1) {
    console.log("⚠️ User not found:", userId);
    return res.status(404).json({ message: 'User not found' });
  }

  const deletedUser = users.splice(index, 1)[0];
  console.log("🗑️ User deleted:", deletedUser);

  res.status(200).json({ message: 'User deleted successfully', user: deletedUser });
});

const PORT = 3000;
app.listen(PORT, () => console.log(`🚀 Servidor corriendo en http://192.168.1.4:${PORT}`));
