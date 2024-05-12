package com.example.chatbot;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private EditText messageEditText;
    private Button sendButton;
    private TextView chatHistoryTextView;

    private Retrofit retrofit;
    private ChatService chatService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);
        chatHistoryTextView = findViewById(R.id.chat_history_text_view);

        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder().readTimeout(10, java.util.concurrent.TimeUnit.MINUTES).build())
                .build();

        chatService = retrofit.create(ChatService.class);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageEditText.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendMessage(message);
                    messageEditText.setText("");
                }
            }
        });
    }

    private void sendMessage(String message) {
        // Get the current chat history from the TextView
        String chatHistoryText = chatHistoryTextView.getText().toString();

        // Parse the chat history text into a list of objects
        List<Map<String, String>> chatHistory = parseChatHistory(chatHistoryText);

        // Create a new ChatRequest object with the user message and chat history
        ChatRequest request = new ChatRequest(message, chatHistory);

        Log.d("ChatActivity", "Sending command: " + message);

        // Send the message using Retrofit
        chatService.sendMessage(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful()) {
                    ChatResponse chatResponse = response.body();
                    if (chatResponse != null) {
                        // Update the chat history TextView with the response message
                        chatHistoryTextView.append("User: " + request.getUserMessage() + "\n");
                        chatHistoryTextView.append("Llama: " + response.body().getMessage() + "\n");
                    }
                } else {
                    // Handle API call failure
                    Toast.makeText(ChatActivity.this, "Error getting response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                // Handle network errors
                Toast.makeText(ChatActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface ChatService {
        @POST("/chat")
        Call<ChatResponse> sendMessage(@Body ChatRequest request);
    }

    private List<Map<String, String>> parseChatHistory(String chatHistoryText) {
        List<Map<String, String>> history = new ArrayList<>();

        // Split the chat history text by newline characters
        String[] lines = chatHistoryText.split("\n");

        // Iterate over each line
        for (String line : lines) {
            // Split the line by ": "
            String[] parts = line.split(": ");

            // Ensure that the line contains both user message and llama response
            if (parts.length >= 2) {
                // Extract user message and llama response
                String userMessage = parts[1];
                String llamaResponse = (parts.length > 2) ? parts[3] : "";

                // Add user message and llama response to the chat history
                Map<String, String> messageMap = new HashMap<>();
                messageMap.put("User", userMessage);
                messageMap.put("Llama", llamaResponse);
                history.add(messageMap);
            }
        }

        return history;
    }

    public static class ChatRequest {
        private String userMessage;
        private List<Map<String, String>> chatHistory;

        public ChatRequest(String userMessage, List<Map<String, String>> chatHistory) {
            this.userMessage = userMessage;
            this.chatHistory = chatHistory;
        }

        public String getUserMessage() {
            return userMessage;
        }
    }

    public static class ChatResponse {
        private String message;

        public String getMessage() {
            return message;
        }
    }
}
