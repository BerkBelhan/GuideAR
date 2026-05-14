using UnityEngine;
using UnityEngine.AI;

public class PathVisualizer : MonoBehaviour
{
    public NavMeshAgent agent; // Attach to a hidden agent or the player
    public LineRenderer pathLine;
    public Transform destination;

    void Update()
    {
        if (destination != null)
        {
            NavMeshPath path = new NavMeshPath();
            if (NavMesh.CalculatePath(transform.position, destination.position, NavMesh.AllAreas, path))
            {
                pathLine.positionCount = path.corners.Length;
                pathLine.SetPositions(path.corners);
            }
        }
    }
}