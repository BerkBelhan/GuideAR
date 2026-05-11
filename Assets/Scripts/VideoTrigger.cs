using UnityEngine;
using UnityEngine.Video;

public class VideoTrigger : MonoBehaviour
{
    public VideoPlayer myVideo; // Drag your Video Player here in Inspector

    // This runs when the Quest 3 headset enters the box
    private void OnTriggerEnter(Collider other)
    {
        // Only play if the thing entering is the Player
        if (other.CompareTag("MainCamera")) 
        {
            myVideo.Play();
        }
    }

    // Optional: Stop the video when they walk away
    private void OnTriggerExit(Collider other)
    {
        if (other.CompareTag("MainCamera"))
        {
            myVideo.Pause();
        }
    }
}