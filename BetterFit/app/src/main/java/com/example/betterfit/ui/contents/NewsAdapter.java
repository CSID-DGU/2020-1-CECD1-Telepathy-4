package com.example.betterfit.ui.contents;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.betterfit.R;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    List<Articles> ArticleList;

    public NewsAdapter(List<Articles> ArticleList) {
        this.ArticleList = ArticleList;
    }

    @Override public NewsAdapter.NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from( parent.getContext()).inflate(R.layout.fragment_article, parent, false);
        return new NewsAdapter.NewsViewHolder(view);
    }

    @Override public void onBindViewHolder(NewsAdapter.NewsViewHolder holder, int position) {
        holder.thumbnail.setImageResource(ArticleList.get(position).getThumbnail());
        holder.newsTitle.setText(ArticleList.get(position).getNewsTitle());
        holder.author.setText(ArticleList.get(position).getAuthor());
        holder.sentence.setText(ArticleList.get(position).getSentence());
        //holder.videoWeb.loadData( youtubeVideoList.get(position).getVideoUrl(), "text/html" , "utf-8" );
    }

    @Override public int getItemCount() {
        return ArticleList.size();
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder {
        protected ImageView thumbnail;
        protected TextView newsTitle;
        protected TextView author;
        protected TextView sentence;

        public NewsViewHolder(View view) {
            super(view);
            this.thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            this.newsTitle = (TextView) view.findViewById(R.id.newsTitle);
            this.author = (TextView) view.findViewById(R.id.author);
            this.sentence = (TextView) view.findViewById(R.id.sentence);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    Articles article = ArticleList.get(pos);
                    String url = article.url;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                    v.getContext().startActivity(intent);
                }
            });
        }
    }
}
