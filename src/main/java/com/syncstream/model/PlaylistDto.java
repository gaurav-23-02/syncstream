package com.syncstream.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistDto {
    private String id;
    private String title;
    private String description;
    private String owner;
    private Integer totalTracks;
    private String imageUrl;
    private String uri;
    private Boolean isPublic;
    private Boolean collaborative;
    private List<TrackDto> tracks;
}
