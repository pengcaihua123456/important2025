package com.example.design.share.use;

import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.design.share.MessageType;

import java.util.List;


// 在RecyclerView.Adapter中使用
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private List<ChatMessage> messages;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MessageType type = MessageType.values()[viewType];
        return new ViewHolder(new ChatBubbleView(parent.getContext(), type, ""));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        holder.bubbleView.updateContent(msg.content);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ChatBubbleView bubbleView;

        ViewHolder(ChatBubbleView view) {
            super(view);
            bubbleView = view;
        }
    }


}
