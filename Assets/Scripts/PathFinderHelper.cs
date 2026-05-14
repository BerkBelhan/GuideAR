using UnityEngine;
using UnityEngine.AI;

public class PathfinderHelper : MonoBehaviour
{
    public Transform playerTransform; // Assign Main Camera or XR Rig
    public LineRenderer pathLine;
    public Transform currentTarget; // The destination (e.g., Room 101)
    
    private NavMeshPath path;

    void Start()
    {
        path = new NavMeshPath();
    }

    void Update()
    {
        if (currentTarget != null)
        {
            DrawPath();
        }
    }

    void DrawPath()
    {
        // Calculate path from Player to Target
        if (NavMesh.CalculatePath(playerTransform.position, currentTarget.position, NavMesh.AllAreas, path))
        {
            pathLine.positionCount = path.corners.Length;
            
            // Draw lines between all corners found by NavMesh
            for (int i = 0; i < path.corners.Length; i++)
            {
                // Slightly offset Y so the line doesn't "flicker" into the floor
                Vector3 point = path.corners[i] + Vector3.up * 0.05f;
                pathLine.SetPosition(i, point);
            }
        }
    }

    // Call this from your UI Buttons to change destinations
    public void SetDestination(Transform newTarget)
    {
        currentTarget = newTarget;
    }
}