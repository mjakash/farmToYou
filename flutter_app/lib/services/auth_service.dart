import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class AuthService {
  // CRITICAL: You CANNOT use 'localhost' or '127.0.0.1'
  // Use your computer's local IP address.
  // On Windows, find it with 'ipconfig' in cmd.
  // On Mac/Linux, find it with 'ifconfig' or 'ip a' in terminal.
  final String _baseUrl = "http://10.0.2.2:8080/api/users"; // <-- CHANGE THIS

  final _storage = const FlutterSecureStorage(); 

  static const _tokenKey = 'authToken'; 

  Future<Map<String, dynamic>> login({
    required String email,
    required String password,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$_baseUrl/login'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'email': email,
          'password': password,
        }),
      );

      final responseBody = jsonDecode(response.body);

      if (response.statusCode == 200) {
        // --- THIS IS THE CRITICAL PART ---
        // Login is successful, save the token
        String token = responseBody['token'];
        await _storage.write(key: _tokenKey, value: token);
        
        return {'success': true, 'message': 'Login successful!'};
      } else {
        // Handle backend errors (like "Invalid email or password")
        return {
          'success': false,
          'message': "Error: ${responseBody['message'] ?? 'Invalid credentials'}"
        };
      }
    } catch (e) {
      // Handle network errors
      return {
        'success': false,
        'message': "Error: Could not connect to the server. ${e.toString()}"
      };
    }
  }

  Future<String> register({
    required String name,
    required String email,
    required String password,
    required String phone,
    required String address,
    required String role, // "CUSTOMER", "FARMER", or "DELIVERY_AGENT"
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$_baseUrl/register'),
        headers: {
          'Content-Type': 'application/json',
        },
        body: jsonEncode({
          'name': name,
          'email': email,
          'password': password,
          'phone': phone,
          'address': address,
          'role': role,
        }),
      );

      final responseBody = jsonDecode(response.body);

      if (response.statusCode == 201) {
        // Success
        return "Registration successful!";
      } else {
        // Handle backend errors (like "Email Already Registered")
        return "Error: ${responseBody['message'] ?? 'Unknown error'}";
      }
    } catch (e) {
      // Handle network errors (like "Connection refused")
      return "Error: Could not connect to the server. ${e.toString()}";
    }
  }

  // We will add the login function here later
}