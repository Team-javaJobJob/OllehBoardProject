package com.example.ollethboardproject.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommunityCreateRequest {
    private String region;
    private String interest;
    private String info;
    private String communityName;
}
